import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {


   public static void main(String[] args) {
      Random seedgen = new Random();
      int seed = seedgen.nextInt();
      String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
      Random tokenRandom = new Random(seed);
      String token = generateToken(tokenRandom, charset);

      System.out.println("Generated token: " + token);
      System.out.println("Actual seed: " + seed);
      System.out.println("Charset length: " + charset.length());

      System.out.println(
            "Running randcrack_mt... The seed that is output is the internal seed of the Random object. So, the one we set after it is scrambled.");
      // run ./randcrack_mt -algorithm "LCG" -method "nextIntn" -missing 0 -probn 52 -values "<randomNumbers>"
      // this is the "internal seed" of the Random object, not the one we set
      Long crackedSeed = crackSeed(token, charset);
      if (crackedSeed == null) {
         System.out.println("Failed to crack the seed.");
         return;
      }

      System.out.println(System.nanoTime());
      MyDirectSeedRandom crackedRandom = new MyDirectSeedRandom(crackedSeed);
      System.out.println(System.nanoTime());

      String crackedToken = generateToken(crackedRandom, charset);

      // FIXME: cracked token starts one character later than the original token
      System.out.println("Original token: " + token);
      System.out.println("Cracked token: " + crackedToken);

      // next token
      System.out.println("Next token: " + generateToken(tokenRandom, charset));
      System.out.println("Next cracked token: " + generateToken(crackedRandom, charset));
   }

   private static String generateToken(Random rand, String charset) {
      StringBuilder res = new StringBuilder();
      for (int i = 0; i <= 32; i++) {
         int randIndex = rand.nextInt(charset.length());
         res.append(charset.charAt(randIndex));
      }
      return res.toString();
   }

   private static Long crackSeed(String token, String charset) {
      // reverse the integers from the string
      List<Integer> randomNumbers = new ArrayList<>();
      for (int i = 0; i < token.length(); i++) {
         randomNumbers.add(charset.indexOf(token.charAt(i)));
      }
      System.out.println("Random numbers of the token(s): " + randomNumbers);

      Long crackedSeed = null;
      try {
         ProcessBuilder processBuilder =
               new ProcessBuilder("./randcrack_mt", "-algorithm", "LCG", "-method", "nextIntn", "-next", "10", "-missing",
                     "0", "-probn", "52", "-values",
                     randomNumbers.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElseThrow());
         processBuilder.directory(new File("src"));
         Process process = processBuilder.start();

         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
         String line;
         while ((line = reader.readLine()) != null) {
            System.out.println(line);
            // line looks like "seed: 1234567890. The next 10 values after 30 are:.
            if (line.startsWith("seed: ")) {
               crackedSeed = Long.parseLong(line.substring(6, line.indexOf('.')));
               System.out.println("Cracked seed: " + crackedSeed);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return crackedSeed;
   }

   /**
    *
    * TODO: idk if this can ever work
    * Step 1: Bruteforce the seedUniquifier from the known internal seed and a guessed nano time.
    * Step 2: Bruteforce the seed from the next seedUniquifier and the guessed nano time.
    */
   private static void crackNextRandom(int crackedSeed, int approxNanoTime) {
      // Assuming you have the internal seed extracted
      long internalSeed = crackedSeed; // Replace with the actual cracked internal seed

      // Determine the range of nano time to bruteforce
      long startNanoTime = approxNanoTime - 1_000_000L; // Adjust the range as needed
      long endNanoTime = approxNanoTime + 1_000_000L; // Adjust the range as needed
      long seedUniquifierMultiplier = 1181783497276652981L;
   }
}
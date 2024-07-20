import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

// sets the initial seed directly
public class MyDirectSeedRandom extends Random {

   private static final long multiplier = 0x5DEECE66DL;
   private static final long addend = 0xBL;
   private static final long mask = (1L << 48) - 1;
   private final AtomicLong seed = new AtomicLong();

   public MyDirectSeedRandom(long seed) {
      this.seed.set(seed);
   }

   @Override
   public int nextInt(int bound) {

      int r = next(31);
      int m = bound - 1;
      if ((bound & m) == 0)  // i.e., bound is a power of 2
      {
         r = (int) ((bound * (long) r) >> 31);
      } else {
         for (int u = r;
               u - (r = u % bound) + m < 0;
               u = next(31))
            ;
      }
      return r;
   }

   @Override
   public int next(int bits) {
      long oldseed, nextseed;
      do {
         oldseed = seed.get();
         nextseed = (oldseed * multiplier + addend) & mask;
      } while (!seed.compareAndSet(oldseed, nextseed));
      return (int) (nextseed >>> (48 - bits));
   }
}

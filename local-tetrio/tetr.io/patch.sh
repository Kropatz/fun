#!/usr/bin/env bash

# Path to the file to be patched
FILE="js/tetrio.js"

# Define patches as an array of search-replace pairs, separated by a unique delimiter (e.g., ':::')
PATCHES=(
  'domain:"tetr.io":::domain:"localhost"'
  'if(_.domain):::if(false&&_.domain)'
)

# Function to safely escape strings for use in sed
escape_string() {
  echo "$1" | sed -e 's/[&/\]/\\&/g'
}

# Function to apply all patches
patch() {
  for patch_pair in "${PATCHES[@]}"; do
    # Split the patch into search and replace strings
    SEARCH_STRING=$(echo "$patch_pair" | awk -F ':::' '{print $1}')
    REPLACE_STRING=$(echo "$patch_pair" | awk -F ':::' '{print $2}')

    # Escape the strings for safe use in sed
    ESCAPED_SEARCH=$(escape_string "$SEARCH_STRING")
    ESCAPED_REPLACE=$(escape_string "$REPLACE_STRING")

    # Apply the patch
    if grep -q "$SEARCH_STRING" "$FILE"; then
      sed -i "s|$ESCAPED_SEARCH|$ESCAPED_REPLACE|g" "$FILE"
      echo "Applied patch: '$SEARCH_STRING' -> '$REPLACE_STRING' in $FILE"
    else
      echo "Patch not applied: '$SEARCH_STRING' not found in $FILE"
    fi
  done
}

# Function to revert all patches
unpatch() {
  for patch_pair in "${PATCHES[@]}"; do
    # Split the patch into search and replace strings
    SEARCH_STRING=$(echo "$patch_pair" | awk -F ':::' '{print $1}')
    REPLACE_STRING=$(echo "$patch_pair" | awk -F ':::' '{print $2}')

    # Escape the strings for safe use in sed
    ESCAPED_SEARCH=$(escape_string "$SEARCH_STRING")
    ESCAPED_REPLACE=$(escape_string "$REPLACE_STRING")

    # Revert the patch
    if grep -q "$REPLACE_STRING" "$FILE"; then
      sed -i "s|$ESCAPED_REPLACE|$ESCAPED_SEARCH|g" "$FILE"
      echo "Reverted patch: '$REPLACE_STRING' -> '$SEARCH_STRING' in $FILE"
    else
      echo "Unpatch not applied: '$REPLACE_STRING' not found in $FILE"
    fi
  done
}

# Main script logic to call patch or unpatch based on arguments
case "$1" in
  patch)
    patch
    ;;
  unpatch)
    unpatch
    ;;
  *)
    echo "Usage: $0 {patch|unpatch}"
    ;;
esac


#!/usr/bin/env bash

# Extract the URIs and download the files, preserving directory structure
grep '404' caddylog | sed -n 's/.*"uri":\s*"\([^"]*\)".*/https:\/\/tetr.io\1/p' | while read url; do
  # Remove the base URL (https://tetr.io) to get the relative file path
  file_path=$(echo "$url" | sed 's|https://tetr.io||')

  # Ensure the directory structure is created in the current working directory
  mkdir -p "./$(dirname "$file_path")"

  # Download the file using wget
  wget --no-parent -P "./$(dirname "$file_path")" "$url"
done


# i-forgit
i forgor anyways we're making git or something for honors topics

## init()
initialize's the Git repository

## hashFile(String filePath)
- Passes in the filePath of the file to be hashed
- Reads contents of file into a StringBuilder
- Turns the contents into bytes
- Hashes bytes with SHA-1
- Returns hashed file contents

## createBlob(String filePath) 
- gets hash of file using hashFile(String filePath)
- creates file in "/objects"
- Path.copy the original file content (bytes) to the new file


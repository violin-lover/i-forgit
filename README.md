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

## updateIndex (String filePath)
- reads entire existing index file into ArrayList<> lines
- if matching filePath is found, it replaces with new content/entry (if there is)
- else it creates an entirely new entry into index

## samePath (String line, String filePath)
- checks if the line (from index) being passed in has the same path as filePath by splicing in the middle (at the space)

## 10/22/26 fixes
- checkout delete every tracked file + parent folder, even if files exist in commit being checked out --> saved old index, rebuilt index for target commit, then compared to see if file needs to be deleted
- checkout updated HEAD without restoring file contents --> added loop to copy each blob from git/objects back to its OG file path, creating folders as necessary; HEAD updated at end
- edited printCommit() so it updates curr branch instead of HEAD 

## createBranch()
- creates branch file without changing head
- Git.createBranch("branchName");
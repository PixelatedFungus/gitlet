# Gitlet Design Document

**Name**: Wei Chen Chu (Wilson)

## Classes and Data Structures
* Main
  * No instance variables so far
  * This class will encompass what I need for running the general program
* Init
  * Creates the files needed for us to save persistent objects
* Add
  * Uses serialization to create a saved version of a file
* Commit
  * Adds whatever is in the staging area to the history area in our .gitlet folder
  * Should have many fields representing a file 
* Branch
  * Instance variable such as name of the branch
    * Name
      * Instance variable for the name of our branch
* BranchUser
  * Keeps track of which "user" is accessing a branch
* UserInput
  * Verifies the validity of a user's input
* Remove
  * Maybe an abstract class
  * Subclass rm which removes files
  * Subclass rm-branch which removes branches
* Global-Log
  * Subclass Log
    * Should display some commit history
  * Maybe have FIND as a method here
* Status
* Checkout
  * Subclass Reset
* Merge
  * More difficult to implement and we may need subclasses that are still unknown
* History
  * Each time an user starts gitlet, History should have ways to access the persistent files inside of .gitlet
* StagingArea
  * Accesses file(s) that are persistent representations of what we have added to the staging area
* File
  * Represents a file being created with instance variables such as the date and time the file was made

## Algorithms
* Init - create the proper files described in Persistence
* Merge:
  * Check if we need to merge
  * Check if the merge we are trying to do has conflicts
    * How would we do this?
    * If it does have merge conflicts, how do we represent it?
  * Actually merge the files if there are no merge conflicts
* Reset:
  * Untrack all files from the staging area
  * Essentially checkout, so we might want to look into how to adapt that function for reset
* rm-branch:
  * Approach this function with caution
  * Should remove the pointer to a branch, so the branch no longer exists when we run the branch function but the commits created under the branch should not disappear
* branch:
  * create a new file for the branch to store its commits and everything


## Persistence
* At the least, make sure that .gitlet has folders:
  * branches
    * should store the branches that we have
    * stagingArea
      * Should store the files being staged from our "add" function
    * history
      * Should store saved hashcodes of our files
    * commit
      * Should store the history of our commits

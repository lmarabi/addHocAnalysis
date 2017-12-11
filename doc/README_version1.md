# Version 1 : Spatial addhoc analysis 


## Summary 

**Spatial Quadtree**
Spatial quadtree is a unbalanced quadtree, that divide the space into four quadrants. In the developed quadtree we fixed the depth of the tree to 16 because when we zoom into the google maps there is no need zoom more than 16 depth of the quadtree. 

**Promosis for the first version** 
The first version will cover the entire 2015 twitter dataset. 


**Branches in the git repository**
levesLevel: Only quadtree leves contains the inverted index, The base line implementation From C.Ynsin paper. 
newIdea: The keyword search is materialized based on a temporal keyword inverted index on the disk. 



## How to create branch and push. 

--First to create a branch then you need to create a branch using the following command line--
$git branch <nameofBranch>

--Second to work on a spesific branch then you need to check out the branch--
$git checkout <nameofBranch>

--Third to submit changes of a branch then you need to do the following--
$git push -u origin <nameofBranch>

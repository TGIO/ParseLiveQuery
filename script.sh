#Checkout
git checkout --orphan latest_branch

#Add all the files
git add -A

#Commit the changes
git commit -am "init"

#Delete the branch
git branch -D master

#Rename the current branch to master
git branch -m master

#Finally, force update your repository
git push -f origin master
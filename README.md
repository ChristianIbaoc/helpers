What's up?
This is a repository that contains helper files for QA.
To access the package in a maven way, follow these steps:

1.) Generate your OWN PAT in Github
    - Profile --> Dev Settings --> PAT
    - Assign it with read packages scope

2.) Go to your user folder C:/Users/<kimi-no-namae-wa>
3.) Find the ".m2" folder and edit/create the "settings.xml" file.
4.) Add this block:

    <servers>
     <server>
       <id>github</id>
       <username>YOUR_USERNAME</username>
       <password>YOUR_PAT</password>
     </server>
   </servers>

5.) You should be good
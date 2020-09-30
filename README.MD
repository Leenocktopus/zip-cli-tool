## Java CLI zip archiver 

#### General

Java CLI zip archiver is a very basic tool to compress and decompress files on 
Windows operating system. It has not been tested on other operating systems and 
thus it's usage may produce unexpected results. 
Inspiration for this project was drawn from UNIX-like systems 
[zip command](https://linux.die.net/man/1/zip).


Upon download, it can be packaged into JAR with `mvn package`. In order to
run it, standard java command can be used: `java -jar [path to the JAR] [arguments]`.

In order to use it conveniently, batch file with the following code can 
be created (keep in mind, that instead of `zip-0.0.3.jar` you should 
provide an absolute path if batch file location isn't the `target` folder):

````batch
@java -jar zip-0.0.3.jar %*
````
A folder with the batch file can be added to the `PATH` environment variable 
in order to use `zip` command regardless of the current directory in cmd.

#### Usage
Control over the specific operation and it's parameters is performed by supplying
command arguments to the `zip` command. General structure of the commands are as follows:


`zip [command]`

Where available commands are: 

|argument|command|
|---|---|
|-help|print information about commands to the output stream|
|-version|print product version and developer to the output stream and exit|

and, 


`zip [options] [zip archive] [filename1, filename2, ...]`

Where main options are: 

|argument|option|
|---|---|
|-a| create archive with the name provided as [zip-archive] and contents provided as [filename1, filename2, ...]|
|-n| unpack archive with the name provided as [zip-archive] and destination provided as [filename]. Using this option without destination will unpack files to the current directory. Multiple destinations produce exception.|
|-u| update archive with the name provided as [zip-archive] and contents provided as [filename1, filename2, ...]|
|-d| remove files provided as [filename1, filename2, ...] from archive provided [zip-archive]. Each filenameshould be an absolute path in the archive.|

Followed by one of available optional arguments: 


|argument|option|
|---|---|
|-verbose|enable verbose output|
|-debug|enable debug output|



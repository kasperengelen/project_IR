# Project Information Retrieval (2019-2020)

This is our project that we made for the course Information Retrieval. In this project we had index a large amount of files so that it was possible to query information from the dataset.

## Command line arguments

* ```mode```: can be "normal" for normal search and "rocchio" for relevance feedback. Default is "normal".
* ```index```: can be "true" to perform index, can be "false" to not perform index. The program will prompt the user if omitted.
* ```progress```: can be "true" to print indexing progress, can be "false" not to print indexing progress. Default is "true".
* ```top```: pass integer value. The value will be the amount of documents returned during search. Default is 10.

## Constants

There is a ```Constants``` class that contains the entries ```PATH_INDEX``` and ```PATH_DOCUMENTS```. These need to be set to point to the folder containing the index and the documents respectively.

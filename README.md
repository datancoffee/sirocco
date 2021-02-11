# Sirocco Opinion Extraction Framework

The Sirocco opinion extraction framework was first developed at [Cuesense](http://cuesense.com) in early 2010s. 
Since then it was updated and maintained by Sergei Sokolenko [@datancoffee](https://twitter.com/datancoffee). You can follow the news on Sirocco on [Twitter](https://twitter.com/datancoffee) and [Medium](https://medium.com/@datancoffee).

Sirocco parses input text (e.g. news articles or threaded conversations on Twitter or Reddit) into subjects and opinions. The subject and the opinion, together with the author of the news article, form a triad that allows us to answer the question: “Who (author) thinks about what (the subject) in what way (the opinion)?”

The theoretical underpinnings of Sirocco are based on a framework of human emotions originally developed by Robert Plutchik, a professor at the Albert Einstein College of Medicine. Plutchik's [Wheel of Emotions](https://en.wikipedia.org/wiki/Contrasting_and_categorization_of_emotions) identifies 8 basic emotions: Joy, Acceptance, Fear, Surprise, Sadness, Disgust, Anger, Anticipation. 

In our research and commercial application of Plutchik we were able to use signals for Anticipation (aka Interest) for lead generation, positive emotion of Joy to extract user testimonials, and negative emotions of Sadness, Anger, and Disgust to identify potential leads for objects that compete with the subjects of the emotion. 

Sirocco relies on [Apache OpenNLP](https://opennlp.apache.org/) to supply it with constituency-based parse trees of sentences.

## Sirocco in Presentations, News and Customer Use-Cases

Sirocco was used by a major pharma company as a solution for automatically identifying high-quality consumer reviews and resurfacing them on a product page. 

We collaborated with Kalev Leetaru from [The GDELT Project](https://www.gdeltproject.org/) / Georgetown University and published a 3-part blog series ([Part 1](https://cloud.google.com/blog/big-data/2018/03/predicting-community-engagement-on-reddit-using-tensorflow-gdelt-and-cloud-dataflow-part-1), [Part 2](https://cloud.google.com/blog/big-data/2018/03/predicting-community-engagement-on-reddit-using-tensorflow-gdelt-and-cloud-dataflow-part-2), and [Part 3](https://cloud.google.com/blog/big-data/2018/03/predicting-community-engagement-on-reddit-using-tensorflow-gdelt-and-cloud-dataflow-part-3)) on sentiment analysis in News using Sirocco.

We co-presented ([video recording](https://www.youtube.com/watch?v=tKISLQ87GO8), [slides](https://www.linkedin.com/posts/ssokolenko_cloudnext18da205predicting-reddit-community-activity-6758157371895287808-NyMC)) together with Reddit’s VP of Engineering Nick Caldwell at Cloud Next’18 on “Predicting user engagement at Reddit” and then developed Deep Learning prediction models for Reddit ([blog](https://medium.com/google-cloud/predicting-user-engagement-with-news-on-reddit-using-kaggle-or-colab-d5ef0dcaff6a), [kaggle notebook](https://www.kaggle.com/datancoffee/predicting-reddit-community-engagement-dataset)). 

Sirocco was featured at Strata NYC 2017: 
- In the keynote “Emotional arithmetic: How machine learning helps you understand customers in real time” by Chad Jennings from Google. Chad showed the results of emotion analysis performed by Sirocco ([Video Recording](https://www.oreilly.com/ideas/emotional-arithmetic-how-machine-learning-helps-you-understand-customers-in-real-time))
- In the follow-up deep dive [“Emotional arithmetic: A deep dive into how machine learning and big data help you understand customers in real time”](https://conferences.oreilly.com/strata/strata-ny/public/schedule/detail/63620) by Chad Jennings (Google) and Eric Schmidt (Google) ([Video Recording](https://www.safaribooksonline.com/library/view/strata-data-conference/9781491976326/video314135.html))
 

## Using Sirocco
You can use Sirocco either by running the Sirocco Indexer application locally on your machine or by embedding the Sirocco library in your backend application. It is usually a good idea to start the Sirocco evaluation by making it work locally and running the provided test cases. After reviewing the output results produced by the Indexer tool, and understanding how you can use Sirocco functionality in your system, you can add the Sirocco library to your backend app. While the Sirocco library is written in Java, there are frameworks now that will allow you using it in data pipelines written in Python, Go, and even SQL. The Apache Beam SDK is one of such tools, and the Google Cloud Dataflow service in the Google Cloud allows you operating Sirocco in a fully managed and serverless way. Review the [Dataflow Opinion Analysis](https://github.com/GoogleCloudPlatform/dataflow-opinion-analysis) project for examples of how to embed Sirocco in ETL pipelines running in Cloud Dataflow.


### How to run Sirocco on your local machine
The steps for configuring and running the Sirocco Indexer locally are as follows:

- Install tools necessary for compiling and deploying the code in this project.
- Download and install the latest Opinion Model files
- Clone the Sirocco repo to local machine
- Build Sirocco
- Run the tests


#### Installing Prerequisites

Install tools necessary for compiling and deploying the code in this sample, if not already on your system, specifically git,  Java and Maven:

* Install [`git`](https://git-scm.com/downloads). If you have Homebrew, the command is
```
brew install git
```

* Download and install the [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 1.8 or later. Verify that the JAVA_HOME environment variable is set and points to your JDK installation.

* [Download](http://maven.apache.org/download.cgi) and [install](http://maven.apache.org/install.html) Apache Maven. With Homebrew, the command is:
```
brew install maven
```

#### Install Latest Opinion Model Files 

The [Sirocco Model Files repo](https://github.com/datancoffee/sirocco-mo/releases) contains all the recent releases of model files. You can also find the source files used for bulding the model jar. Download the latest sirocco-mo-x.y.z.jar on the release page of the repo.

* Go to the directory where the downloaded sirocco-mo-x.y.z.jar file is located.

* Install the Sirocco model file in your local Maven repository. Replace x.y.z with downloaded version.

```
mvn install:install-file \
  -DgroupId=sirocco.sirocco-mo \
  -DartifactId=sirocco-mo \
  -Dpackaging=jar \
  -Dversion=x.y.z \
  -Dfile=sirocco-mo-x.y.z.jar \
  -DgeneratePom=true
```


#### Clone Sirocco repo to local machine

To clone the GitHub repository to your computer, run the following command:

```
git clone https://github.com/datancoffee/sirocco
```

Go to the `sirocco` directory. The exact path depends on where you placed the directory when you cloned the sample files from
GitHub.

```
cd sirocco
```


#### Building Sirocco

You need to have Java 8 and Maven 3 installed.

Build and install the Sirocco library
```
mvn clean install
```

Alternatively, run the package and install steps separately (don't forget to replace x.y.z with the active version of Sirocco)
```
mvn clean package
mvn install:install-file -Dfile=target/sirocco-sa-x.y.z.jar -DpomFile=pom.xml
```

The build process will create a shaded jar sirocco-sa-x.y.z.jar that contains all dependencies (including OpenNLP packages) in the target directory. This jar does not contain model files and these need to be downloaded separately (see next step). 


#### Running Included Test Datasets and Your Own Tests

This repo contains a set of blog posts and other text documents that can be used as inputs for verification of changes. These test datasets are located in the **src/test/resources/testdatasets** folder. 

The test scripts that invoke the Sirocco Indexer are located in the **src/test/scripts/** folder. The **runindexer.sh** script processes files with **.txt** extensions while the **runindexercsv.sh** script processes **.csv** files. Both scripts run the indexer through all files of their associated extensions in the **src/test/resources/in** folder (configurable in the script) and produce outputs in the **src/test/resources/out** folder (also configurable in the script). You can use these scripts to run the indexer on *your own data* as well (just make sure to put your files in the input directory specified in the script).

##### Preparing input and output folders and making scripts executable

To do a test run, copy the following test datasets into the input folder.

```
cp -r src/test/resources/testdatasets/articles-col1 src/test/resources/in
cp -r src/test/resources/testdatasets/kaggle-rotten-tomato src/test/resources/in
```

You also need to create output folders, and their names need to match the names of the input folders.
```
mkdir src/test/resources/out
mkdir src/test/resources/out/articles-col1
mkdir src/test/resources/out/kaggle-rotten-tomato
```

Lastly, make sure that the test scripts are executable
```
chmod +x src/test/scripts/*.sh
```

##### Processing bag-of-properties TXT files
For our first test run, let's process TXT files in the **articles-col1** folder. The **runindexer.sh** script expects .txt files to have the following format.

```
Title=<Title>
Author=<Author> 
PubTime=<yyyy-mm-dd hh:mm:ss>
Url=<Url>
Language={EN | UN}

<Text of the article>
```
You can have multiple documents in a .txt file, but they need to be separated by a special separator, which by default is ASCII character 30 (RS). You can change the separator if you modify the script.

To run the test script, execute the following command in shell 

```
src/test/scripts/runindexer.sh TOPSENTIMENTS SHALLOW
```

The results of Sirocco indexing can be reviewed in the src/test/resources/out/articles-col1 folder
```
ls src/test/resources/out/articles-col1
```

The runindexer.sh test script accepts two parameters - the **Indexing Type** and **Parsing Type**. For indexing type the acceptable values are FULLINDEX and TOPSENTIMENTS. When TOPSENTIMENTS is specified, the Indexer will select the top 4 sentence chunks (a few sequential sentences in text that have the same sentiement valence) in input text and output them in the output file. When FULLINDEX is selected, all sentence chunks will be output. 

For parsing type the acceptable values are DEEP, SHALLOW, DEPENDENCY. Parsing type refers to the type of a language tree and subsequent traversing of that tree when connecting entities with sentiments. DEEP and SHALLOW parsing types will cause Sirocco to use [constituency-based trees](https://en.wikipedia.org/wiki/Parse_tree#Constituency-based_parse_trees) and DEPENDENCY parsing type will lead to Sirrocco using [dependency-based trees](https://en.wikipedia.org/wiki/Parse_tree#Dependency-based_parse_trees).

```
./src/test/scripts/runindexer.sh FULLINDEX DEEP
```
Note that while DEEP parsing provides the best quality associations of entities and sentiments, this parsing mode is still work-in-progress (as of Feb 2021). Currently, the SHALLOW parsing mode is the most tested mode. The DEPENDENCY parsing mode is also work-in-progress.


##### Processing CSV files
For our second test run, let's process CSV files in the **kaggle-rotten-tomato** folder.

If you have .csv files with multiple documents (text pieces) per file, you can process them with the **runindexercsv.sh** script. The CSV file can have an unlimited number of columns, but the indexer will be interested in two specific ones: the column that contains the external ID of the document as well as the column that contains the text. 

Here is an example of a CSV file with two documents, each in its own CSV record.
```
SentenceId,PhraseId,Phrase,Sentiment
6,167,A comedy-drama of nearly epic proportions rooted in a sincere performance by the title character undergoing midlife crisis .,4
179,4684,"Beautifully crafted , engaging filmmaking that should attract upscale audiences hungry for quality and a nostalgic , twisty yarn that will keep them guessing .",4
```

Because of this, when processing CSV files you have to specify 4 parameters: Indexing Type, Parsing Type, Item Id Column Index, and Text Column Index. Here is an example that does Shallow parsing of the above CSV file, and sets the Item ID column Index to 1 (meaning, it's the second column in the file, labelled PhraseId), and specifies the Text column Index as 2 (meaning, as the third column in the file, labelled Phrase).
 
```
./src/test/scripts/runindexercsv.sh FULLINDEX SHALLOW 1 2
```

The results of Sirocco indexing can be reviewed in the src/test/resources/out/kaggle-rotten-tomato folder
```
ls src/test/resources/out/kaggle-rotten-tomato
```


Note the following when processing CSV files:
- The indexer expects that there is a header row. All rows starting with the second one will be processed as documents.
- The column indexes for Item ID and Text columns are zero-based. The first column has the index of 0
- The Item ID does not have to be numeric. The value inside that column will be interpreted as a string 
- The Sirocco indexer is capable of handling text pieces that have line breaks in them. These text pieces need to be put in quotes.


### How to Incorporate Sirocco in Your Project
After running the Sirocco Indexer on included test datasets and your own data and verifying that its output is useful for you, you might decide that you want to embed Sirocco in your own application. 
- You could embed Sirocco into a data processing pipeline that processes large volumes of documents in batch or streaming mode
- Or you could embed Sirocco into an event-driven application that calls Sirocco as documents arrive 

A good example of the first type of Sirocco usage is the [Dataflow Opinion Analysis](https://github.com/GoogleCloudPlatform/dataflow-opinion-analysis) project. But even if you are building an event-driven app, the Dataflow Opinion Analysis project can provide good pointers to the API and its usage.


#### Including the Sirocco library and Sirocco Model Files into your binary

Install the Sirocco library sirocco-sa-x.y.z.jar and Sirocco Model files sirocco-mo-x.y.z.jar

Add the dependency to your project's pom.xml file.

```
<dependency>
  <groupId>sirocco.sirocco-sa</groupId>
  <artifactId>sirocco-sa</artifactId>
  <version>[1.0.0,2.0.0)</version>
</dependency>
```

You will also need to add a dependency to the model files

```
<dependency>
  <groupId>sirocco.sirocco-mo</groupId>
  <artifactId>sirocco-mo</artifactId>
  <version>[1.0.0,2.0.0)</version>
</dependency>
```



## Roadmap

We are actively working on improving the quality of the Sirocco models as well as extending its availability on NLP frameworks. We are looking for contributors for helping us to accomplish this mission. See below for contact info.

* Firebase app for consumers of news to gamefy the evaluation of idioms in context of real sentences. The goal is to improve the sentiment ratings of idioms. Everyone benefits. See the [Model Files repo](https://github.com/datancoffee/sirocco-mo)

* Enrich the Google Cloud NLP API with Plutchik sentiment ratings

* Use the Google Cloud NLP API (in addition to Apache OpenNLP) for tokenization, POS-tagging, and sentence-tree creation. Ultimately, provide Plutchik sentiment for both Cloud NLP and Apache OpenNLP.


## Additional Publications and Documentation


[July 2017 - Life of a Cloud Dataflow service-based shuffle](https://cloud.google.com/blog/big-data/2017/07/life-of-a-cloud-dataflow-service-based-shuffle)

[May 2017 - Designing ETL architecture for a cloud-native data warehouse on Google Cloud Platform](https://cloud.google.com/blog/big-data/2017/05/designing-etl-architecture-for-a-cloud-native-data-warehouse-on-google-cloud-platform)

[May 2017 - Opinion Analysis of Text using Plutchik](https://medium.com/@datancoffee/opinion-analysis-of-text-using-plutchik-5119a80229ea)

[April 2017 - Selecting a Java WordNet API for lemma lookups](https://medium.com/@datancoffee/selecting-a-java-wordnet-api-for-lemma-lookups-7fae7a273f91)

[April 2017 - Modernizing Sirocco from C# and SharpNLP to Java and Apache OpenNLP](https://medium.com/@datancoffee/modernizing-sirocco-from-c-and-sharpnlp-to-java-and-apache-opennlp-56550ee041b6)

## Useful Links

* [Sirocco Model Files](https://github.com/datancoffee/sirocco-mo) github repository 
* [Dataflow Opinion Analysis](https://github.com/GoogleCloudPlatform/dataflow-opinion-analysis) project uses the Sirocco Indexer and a plethora of Google Cloud tools such as Dataflow, Pub/Sub, BigQuery etc. to build an opinion analysis processing pipeline for news, threaded conversations in forums like Hacker News, Reddit, or Twitter and other user generated content.

## Want to Help or Get in Touch?

* Get in touch with @datancoffee on [Twitter](https://twitter.com/datancoffee) or [Medium](http://medium.com/@datancoffee) if you want to help with the project or need help.





# Sirocco Opinion Extraction Framework

The Sirocco opinion extraction framework was first developed at [Cuesense](http://cuesense.com) in early 2010s. 
Since then it was updated and maintained by Sergei Sokolenko [@datancoffee](https://twitter.com/datancoffee). You can follow the news on Sirocco on [Twitter](https://twitter.com/datancoffee) and [Medium](https://medium.com/@datancoffee).

Sirocco parses input text (e.g. news articles or threaded conversations on Twitter or Reddit) into subjects and opinions. The subject and the opinion, together with the author of the news article, form a triad that allows us to answer the question: “Who (author) thinks about what (the subject) in what way (the opinion)?”

The theoretical underpinnings of Sirocco are based on a framework of human emotions originally developed by Robert Plutchik, a professor at the Albert Einstein College of Medicine. Plutchik's [Wheel of Emotions](https://en.wikipedia.org/wiki/Contrasting_and_categorization_of_emotions) identifies 8 basic emotions: Joy, Acceptance, Fear, Surprise, Sadness, Disgust, Anger, Anticipation. 

In our research and commercial application of Plutchik we were able to use signals for Anticipation (aka Interest) for lead generation, positive emotion of Joy to extract user testimonials, and negative emotions of Sadness, Anger, and Disgust to identify potential leads for objects that compete with the subjects of the emotion. 

Sirocco relies on [Apache OpenNLP](https://opennlp.apache.org/) to supply it with constituency-based parse trees of sentences.

## Sirocco in Presentations and News

Strata NYC 2017 keynote 
[Emotional arithmetic: How machine learning helps you understand customers in real time](https://conferences.oreilly.com/strata/strata-ny/public/schedule/detail/63895)
by Chad Jennings from Google showed the results of emotion analysis performed by Sirocco

Sirocco was featured in the Strata NYC 2017 deep dive
[Emotional arithmetic: A deep dive into how machine learning and big data help you understand customers in real time](https://conferences.oreilly.com/strata/strata-ny/public/schedule/detail/63620) by Chad Jennings (Google) and Eric Schmidt (Google)

## Roadmap

We are actively working on improving the quality of the Sirocco models as well as extending its availability on NLP frameworks. We are looking for contributors for helping us to accomplish this mission. See below for contact info.

* Firebase app for consumers of news to gamefy the evaluation of idioms in context of real sentences. The goal is to improve the sentiment ratings of idioms. Everyone benefits. See the [Model Files repo](https://github.com/datancoffee/sirocco-mo)

* Enrich the Google Cloud NLP API with Plutchik sentiment ratings

* Use the Google Cloud NLP API (in addition to Apache OpenNLP) for tokenization, POS-tagging, and sentence-tree creation. Ultimately, provide Plutchik sentiment for both Cloud NLP and Apache OpenNLP.
 

## How to Build Sirocco

You need to have Java 8 and Maven 3 installed.

Build and install the Sirocco library
```
mvn clean install
```

Alternatively, run the package and install steps separately
```
mvn clean package
mvn install:install-file -Dfile=target/sirocco-sa-1.0.0.jar -DpomFile=pom.xml
```

The build process will create a shaded jar sirocco-sa-x.y.z.jar that contains all dependencies (including OpenNLP packages) in the target directory. This jar does not contain model files and these need to be downloaded separately (see next step). 

## How to Get the Latest Model Files 

The [Sirocco Model Files repo](https://github.com/datancoffee/sirocco-mo/releases) contains all the recent releases of model files. You can also find the source files used for bulding the model jar. Download the latest sirocco-mo-x.y.z.jar on the release page of the repo.


## How to Incorporate Sirocco in Your Project

Once you built and installed the Sirocco library sirocco-sa-x.y.z.jar and Sirocco Model files sirocco-mo-x.y.z.jar, add the dependency to your project's pom.xml file.

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

## How to Run Standard Test Cases

This repo contains a set of blog posts and other text documents that can be used as inputs for verification of changes. The main test script that invokes the Sirocco Indexer is located in the /src/test/scripts/ folder. It runs the indexer through all  files with .txt extensions in the /src/test/resources/in folder and produces outputs in the /src/test/resources/out folder. To run the test script, execute the following command in shell 

```
./src/test/scripts/runindexer.sh TOPSENTIMENTS
```

The test script accepts a single parameter - the indexing type. The acceptable values are FULLINDEX and TOPSENTIMENTS. When Top Sentiments is specified, the Indexer will select the top 4 sentence chunks (a few sequential sentences in text that have the same sentiement valence) in input text and output them in the output file. When Full Index is selected, all sentence chunks will be output.

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





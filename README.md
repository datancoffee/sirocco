# Sirocco Opinion Extraction Framework

The Sirocco opinion extraction framework was first developed at [Cuesense](http://cuesense.com) in early 2010s. 
Since then it was updated and maintained by Sergei Sokolenko [@datancoffee](https://twitter.com/datancoffee). You can follow the news on Sirocco on [Twitter](https://twitter.com/datancoffee) and [Medium](https://medium.com/@datancoffee).

Sirocco parses input text (e.g. news articles or threaded conversations on Twitter or Reddit) into subjects and opinions. The subject and the opinion, together with the author of the news article, form a triad that allows us to answer the question: “Who (author) thinks about what (the subject) in what way (the opinion)?”

The theoretical underpinnings of Sirocco are based on a framework of human emotions originally developed by Robert Plutchik, a professor at the Albert Einstein College of Medicine. Plutchik's [Wheel of Emotions](https://en.wikipedia.org/wiki/Contrasting_and_categorization_of_emotions) identifies 8 basic emotions: Joy, Acceptance, Fear, Surprise, Sadness, Disgust, Anger, Anticipation. 

In our research and commercial application of Plutchik we were able to use signals for Anticipation (aka Interest) for lead generation, positive emotion of Joy to extract user testimonials, and negative emotions of Sadness, Anger, and Disgust to identify potential leads for objects that compete with the subjects of the emotion. 

Sirocco relies on [Apache OpenNLP](https://opennlp.apache.org/) to supply it with constituency-based parse trees of sentences.

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
Work in Progress (coming soon).

## How to Incorporate Sirocco in Your Project

Once you built and installed the Sirocco library sirocco-sa-1.0.0.jar, add the dependency to your project's pom.xml file.

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



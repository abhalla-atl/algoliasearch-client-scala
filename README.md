<!--NO_HTML-->

# Algolia Search API Client for Scala

<!--/NO_HTML-->



**WARNING:** Please upgrade to a version >= 1.6.2 as it fixes a bug in the automatic fallback of this client.



<!--NO_HTML-->

[Algolia Search](https://www.algolia.com) is a hosted full-text, numerical, and faceted search engine capable of delivering realtime results from the first keystroke.

<!--/NO_HTML-->

Our Scala client lets you easily use the [Algolia Search API](https://www.algolia.com/doc/rest) from your backend. It wraps the [Algolia Search REST API](https://www.algolia.com/doc/rest).



[![Build Status](https://travis-ci.org/algolia/algoliasearch-client-scala.png?branch=master)](https://travis-ci.org/algolia/algoliasearch-client-scala) [![Coverage Status](https://coveralls.io/repos/algolia/algoliasearch-client-scala/badge.svg?branch=master&service=github)](https://coveralls.io/github/algolia/algoliasearch-client-scala?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.algolia/algoliasearch-scala_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.algolia/algoliasearch-scala_2.11/)



**WARNING:** The JVM does not allow to configure timeouts on DNS resolution, it uses the value of the OS. See: https://github.com/algolia/algoliasearch-client-scala/issues/100



<!--NO_HTML-->

Table of Contents
-----------------
**Getting Started**

1. [Setup](#setup)
1. [Quick Start](#quick-start)
1. [Philosophy of the scala client](#philosophy)1. [Guides & Tutorials](#guides-tutorials)


**Commands Reference**

1. [Add a new object](#add-a-new-object-to-the-index)
1. [Update an object](#update-an-existing-object-in-the-index)
1. [Search](#search)
1. [Multiple queries](#multiple-queries)
1. [Get an object](#get-an-object)
1. [Delete an object](#delete-an-object)
1. [Index settings](#index-settings)
1. [List indices](#list-indices)
1. [Delete an index](#delete-an-index)
1. [Clear an index](#clear-an-index)
1. [Wait indexing](#wait-indexing)
1. [Batch writes](#batch-writes)
1. [Copy / Move an index](#copy--move-an-index)
1. [Backup / Export an index](#backup--export-an-index)
1. [API Keys](#api-keys)
1. [Logs](#logs)


<!--/NO_HTML-->



Setup
============
To setup your project, follow these steps:

If you're using Maven, add the following dependency to your `pom.xml` file:
```xml
<dependency>
    <groupId>com.algolia</groupId>
    <artifactId>algoliasearch-scala_2.11</artifactId>
    <version>[1,)</version>
</dependency>
```

For Snapshots add the Sonatype repository:
```xml
<repositories>
    <repository>
        <id>oss-sonatype</id>
        <name>oss-sonatype</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

Initialize the client with your Application ID and API Key. You can find them on [your Algolia account](https://www.algolia.com/users/edit):

If you're using SBT, add the following dependency to your `build.sbt` file:
```scala
libraryDependencies += "com.algolia" %% "algoliasearch-scala" % "[1,)"
```

For Snapshots add the Sonatype repository:
```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
```

Initialize the client with your Application ID and API Key. You can find them on [your Algolia account](https://www.algolia.com/users/edit):


```scala
val client = new APIClient("YourApplicationID", "YourAPIKey")
```




Quick Start
-------------


In 30 seconds, this quick start tutorial will show you how to index and search objects.

```scala
//For the DSL
import algolia.AlgoliaDsl._

//For basic Future support, you might want to change this by your own ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

//case class of your objects
case class Contact(firstname: String,
                   lastname: String,
                   followers: Int,
                   compagny: String)

val indexing1: Future[Indexing] = client.execute {
    index into "contacts" `object` Contact("Jimmie", "Barninger", 93, "California Paint")
}

val indexing2: Future[Indexing] = client.execute {
    index into "contacts" `object` Contact("Warren", "Speach", 42, "Norwalk Crmc")
}
```

You can now search for contacts using firstname, lastname, company, etc. (even with typos):
```scala
// search by firstname
client.execute { search into "contacts" query Query(query = Some("jimmie")) }

// search a firstname with typo
client.execute { search into "contacts" query Query(query = Some("jimie")) }

// search for a company
client.execute { search into "contacts" query Query(query = Some("california paint")) }

// search for a firstname & company
client.execute { search into "contacts" query Query(query = Some("jimmie paint")) }
```

Settings can be customized to tune the search behavior. For example, you can add a custom sort by number of followers to the already great built-in relevance:
```scala
client.execute {
	changeSettings of "myIndex" `with` IndexSettings(
		customRanking = Some(Seq(CustomRanking.desc("followers")))
	)
}
```

You can also configure the list of attributes you want to index by order of importance (first = most important):
```scala
client.execute {
	changeSettings of "myIndex" `with` IndexSettings(
		attributesToIndex = Some(Seq("lastname", "firstname", "company"))
	)
}
```

Since the engine is designed to suggest results as you type, you'll generally search by prefix. In this case the order of attributes is very important to decide which hit is the best:
```scala
client.execute { search into "contacts" query Query(query = Some("or")) }
client.execute { search into "contacts" query Query(query = Some("jim")) }
```


**Note:** If you are building a web application, you may be more interested in using our [JavaScript client](https://github.com/algolia/algoliasearch-client-js) to perform queries. It brings two benefits:
  * Your users get a better response time by not going through your servers
  * It will offload unnecessary tasks from your servers

```html
<script src="//cdn.jsdelivr.net/algoliasearch/3/algoliasearch.min.js"></script>
<script>
var client = algoliasearch('ApplicationID', 'apiKey');
var index = client.initIndex('indexName');

// perform query "jim"
index.search('jim', searchCallback);

// the last optional argument can be used to add search parameters
index.search(
  'jim', {
    hitsPerPage: 5,
    facets: '*',
    maxValuesPerFacet: 10
  },
  searchCallback
);

function searchCallback(err, content) {
  if (err) {
    console.error(err);
    return;
  }

  console.log(content);
}
</script>
```




Philosophy
==========

DSL
---

The main goal of this client is to provide a human _accessible_ and _readable_ DSL for using Algolia search.

The entry point of the DSL is the [`algolia.AlgoliaDSL` object](src/main/scala/algolia/AlgoliaDsl).
This DSL is used in the `execute` method of [`algolia.AlgoliaClient`](src/main/scala/algolia/AlgoliaClient).

As we want to provide human readable DSL, there is more than one way to use this DSL. For example, to get an object by its `objectID`:
```scala
client.execute { from index "index" objectId "myId" }

//or

client.execute { get / "index" / "myId" }
```

Future
------

The `execute` method always return a [`scala.concurrent.Future`](http://www.scala-lang.org/api/2.11.7/#scala.concurrent.Future).
Depending of the operation it will be parametrized by a `case class`. For example:
```scala
var future: Future[Search] =
    client.execute {
        search into "index" query "a"
    }
```

JSON as case class
------------------
Putting or getting objects from the API is wrapped into `case class` automatically by [json4s](http://json4s.org).

If you want to get objects just search for it and unwrap the result:
```scala
case class Contact(firstname: String,
                   lastname: String,
                   followers: Int,
                   compagny: String)

var future: Future[Seq[Contact]] =
    client
        .execute {
            search into "index" query "a"
        }
        .map { search =>
            search.as[Contact]
        }
```

If you want to get the full results (with `_highlightResult`, etc.):
```scala
case class EnhanceContact(firstname: String,
                          lastname: String,
                          followers: Int,
                          compagny: String,
                          objectID: String,
                          _highlightResult: Option[Map[String, HighlightResult]
                          _snippetResult: Option[Map[String, SnippetResult]],
                          _rankingInfo: Option[RankingInfo]) extends Hit

var future: Future[Seq[EnhanceContact]] =
    client
        .execute {
            search into "index" query "a"
        }
        .map { search =>
            search.asHit[EnhanceContact]
        }
```

For indexing documents, just pass an instance of your `case class` to the DSL:
```scala
client.execute {
    index into "contacts" `object` Contact("Jimmie", "Barninger", 93, "California Paint")
}
```


<!--NO_HTML-->

Guides & Tutorials
================
Check our [online guides](https://www.algolia.com/doc):
 * [Data Formatting](https://www.algolia.com/doc/indexing/formatting-your-data)
 * [Import and Synchronize data](https://www.algolia.com/doc/indexing/import-synchronize-data/scala)
 * [Autocomplete](https://www.algolia.com/doc/search/auto-complete)
 * [Instant search page](https://www.algolia.com/doc/search/instant-search)
 * [Filtering and Faceting](https://www.algolia.com/doc/search/filtering-faceting)
 * [Sorting](https://www.algolia.com/doc/relevance/sorting)
 * [Ranking Formula](https://www.algolia.com/doc/relevance/ranking)
 * [Typo-Tolerance](https://www.algolia.com/doc/relevance/typo-tolerance)
 * [Geo-Search](https://www.algolia.com/doc/geo-search/geo-search-overview)
 * [Security](https://www.algolia.com/doc/security/best-security-practices)
 * [API-Keys](https://www.algolia.com/doc/security/api-keys)
 * [REST API](https://www.algolia.com/doc/rest)


<!--/NO_HTML-->






Add a new object to the Index
==================

Each entry in an index has a unique identifier called `objectID`. There are two ways to add en entry to the index:

 1. Using automatic `objectID` assignment. You will be able to access it in the answer.
 2. Supplying your own `objectID`.

You don't need to explicitly create an index, it will be automatically created the first time you add an object.
Objects are schema less so you don't need any configuration to start indexing. If you wish to configure things, the settings section provides details about advanced settings.

Example with automatic `objectID` assignment:

```scala
val indexing: Future[Indexing] = client.execute {
    index into "contacts" `object` Contact("Jimmie", "Barninger", 93, "California Paint")
}

indexing onComplete {
    case Success(indexing) => println(indexing.objectID)
    case Failure(e) =>  println("An error has occured: " + e.getMessage)
}
```

Example with manual `objectID` assignment:

```scala
val indexing: Future[Indexing] = client.execute {
    index into "contacts" objectId "myID" `object` Contact("Jimmie", "Barninger", 93, "California Paint")
}

indexing onComplete {
    case Success(indexing) => println(indexing.objectID)
    case Failure(e) =>  println("An error has occured: " + e.getMessage)
}
```

Update an existing object in the Index
==================

You have three options when updating an existing object:

 1. Replace all its attributes.
 2. Replace only some attributes.
 3. Apply an operation to some attributes.

Example on how to replace all attributes of an existing object:

```scala
val indexing: Future[Indexing] = client.execute {
    index into "contacts" `object` Contact("Jimmie", "Barninger", 93, "California Paint")
}
```

You have many ways to update an object's attributes:

 1. Set the attribute value
 2. Add a string or number element to an array
 3. Remove an element from an array
 4. Add a string or number element to an array if it doesn't exist
 5. Increment an attribute
 6. Decrement an attribute

Example to update only the city attribute of an existing object:

```scala
client.execute {
	update attribute "city" value "San Francisco" ofObjectId "myId" from "index"
}
```

Example to add a tag:

```scala
client.execute {
	add inAttribute "_tags" value "MyTags" ofObjectId "myId" from "index"
}
```

Example to remove a tag:

```scala
client.execute {
	remove inAttribute "_tags" value "MyTags" ofObjectId "myId" from "index"
}
```

Example to add a tag if it doesn't exist:

```scala
client.execute {
	addUnique inAttribute "_tags" value "MyTags" ofObjectId "myId" from "index"
}
```

Example to increment a numeric value:

```scala
client.execute {
	increment attribute "price" by 42 ofObjectId "myId" from "index"
}
```

Note: Here we are incrementing the value by `42`. To increment just by one, put
`value:1`.

Example to decrement a numeric value:

```scala
client.execute {
	decrement attribute "price" by 42 ofObjectId "myId" from "index"
}
```

Note: Here we are decrementing the value by `42`. To decrement just by one, put
`value:1`.

Search
==================


**Notes:** If you are building a web application, you may be more interested in using our [JavaScript client](https://github.com/algolia/algoliasearch-client-js) to perform queries. It brings two benefits:
  * Your users get a better response time by not going through your servers
  * It will offload unnecessary tasks from your servers.


To perform a search, you only need to initialize the index and perform a call to the search function.

The search query allows only to retrieve 1000 hits, if you need to retrieve more than 1000 hits for seo, you can use [Backup / Retrieve all index content](#backup--retrieve-of-all-index-content)

```scala
client.execute {
	search into "myIndex" query Query(
		query = Some("query string"),
		attributesToRetrieve = Some(Seq("firstname", "lastname")),
		hitsPerPage = Some(50)
	)
}
```

The server response will look like:

```json
{
  "hits": [
    {
      "firstname": "Jimmie",
      "lastname": "Barninger",
      "objectID": "433",
      "_highlightResult": {
        "firstname": {
          "value": "<em>Jimmie</em>",
          "matchLevel": "partial"
        },
        "lastname": {
          "value": "Barninger",
          "matchLevel": "none"
        },
        "company": {
          "value": "California <em>Paint</em> & Wlpaper Str",
          "matchLevel": "partial"
        }
      }
    }
  ],
  "page": 0,
  "nbHits": 1,
  "nbPages": 1,
  "hitsPerPage": 20,
  "processingTimeMS": 1,
  "query": "jimmie paint",
  "params": "query=jimmie+paint&attributesToRetrieve=firstname,lastname&hitsPerPage=50"
}
```

You can use the following optional arguments:

## Full Text Search Parameters
<table><tbody>

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>query</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The instant search query string, used to set the string you want to search in your index. If no query parameter is set, the textual search will match with all the objects.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>queryType</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>prefixLast</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Selects how the query words are interpreted. It can be one of the following values:</p>

<ul>
<li><code>prefixAll</code>: All query words are interpreted as prefixes. This option is not recommended.</li>
<li><code>prefixLast</code>: Only the last word is interpreted as a prefix (default behavior).</li>
<li><code>prefixNone</code>: No query word is interpreted as a prefix. This option is not recommended.</li>
</ul>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>removeWordsIfNoResults</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>none</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>This option is used to select a strategy in order to avoid having an empty result page. There are three different options:</p>

<ul>
<li><code>lastWords</code>: When a query does not return any results, the last word will be added as optional. The process is repeated with n-1 word, n-2 word, ... until there are results.</li>
<li><code>firstWords</code>: When a query does not return any results, the first word will be added as optional. The process is repeated with second word, third word, ... until there are results.</li>
<li><code>allOptional</code>: When a query does not return any results, a second trial will be made with all words as optional. This is equivalent to transforming the AND operand between query terms to an OR operand.</li>
<li><code>none</code>: No specific processing is done when a query does not return any results (default behavior).</li>
</ul>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>minWordSizefor1Typo</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>number</strong></em></div><div><em>Default: <strong>4</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The minimum number of characters in a query word to accept one typo in this word.<br/>Defaults to 4.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>minWordSizefor2Typos</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>number</strong></em></div><div><em>Default: <strong>8</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The minimum number of characters in a query word to accept two typos in this word.<br/>Defaults to 8.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>typoTolerance</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>true</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>This option allows you to control the number of typos allowed in the result set:</p>

<ul>
<li><code>true</code>: The typo tolerance is enabled and all matching hits are retrieved (default behavior).</li>
<li><code>false</code>: The typo tolerance is disabled. All results with typos will be hidden.</li>
<li><code>min</code>: Only keep results with the minimum number of typos. For example, if one result matches without typos, then all results with typos will be hidden.</li>
<li><code>strict</code>: Hits matching with 2 typos are not retrieved if there are some matching without typos.</li>
</ul>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>allowTyposOnNumericTokens</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>true</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to false, disables typo tolerance on numeric tokens (numbers). Defaults to true.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>ignorePlural</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>false</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to true, plural won&#39;t be considered as a typo. For example, car and cars, or foot and feet will be considered as equivalent. Defaults to false.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>disableTypoToleranceOnAttributes</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>[]</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>List of attributes on which you want to disable typo tolerance (must be a subset of the <code>attributesToIndex</code> index setting). Attributes are separated with a comma such as <code>&quot;name,address&quot;</code>. You can also use JSON string array encoding such as <code>encodeURIComponent(&quot;[\&quot;name\&quot;,\&quot;address\&quot;]&quot;)</code>. By default, this list is empty.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>restrictSearchableAttributes</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>attributesToIndex</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>List of attributes you want to use for textual search (must be a subset of the <code>attributesToIndex</code> index setting). Attributes are separated with a comma such as <code>&quot;name,address&quot;</code>. You can also use JSON string array encoding such as <code>encodeURIComponent(&quot;[\&quot;name\&quot;,\&quot;address\&quot;]&quot;)</code>. By default, all attributes specified in the <code>attributesToIndex</code> settings are used to search.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>removeStopWords</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>false</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Remove the stop words from query before executing it. Defaults to false. Contains a list of stop words from 41 languages (Arabic, Armenian, Basque, Bengali, Brazilian, Bulgarian, Catalan, Chinese, Czech, Danish, Dutch, English, Finnish, French, Galician, German, Greek, Hindi, Hungarian, Indonesian, Irish, Italian, Japanese, Korean, Kurdish, Latvian, Lithuanian, Marathi, Norwegian, Persian, Polish, Portugese, Romanian, Russian, Slovak, Spanish, Swedish, Thai, Turkish, Ukranian, Urdu). In most use-cases, we don&#39;t recommend enabling this option.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>advancedSyntax</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>0 (false)</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Enables the advanced query syntax. Defaults to 0 (false).</p>

<ul>
<li><strong>Phrase query</strong>: A phrase query defines a particular sequence of terms. A phrase query is built by Algolia&#39;s query parser for words surrounded by <code>&quot;</code>. For example, <code>&quot;search engine&quot;</code> will retrieve records having <code>search</code> next to <code>engine</code> only. Typo tolerance is <em>disabled</em> on phrase queries.</li>
<li><strong>Prohibit operator</strong>: The prohibit operator excludes records that contain the term after the <code>-</code> symbol. For example, <code>search -engine</code> will retrieve records containing <code>search</code> but not <code>engine</code>.</li>
</ul>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>analytics</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>true</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to false, this query will not be taken into account in the analytics feature. Defaults to true.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>synonyms</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>true</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to false, this query will not use synonyms defined in the configuration. Defaults to true.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>replaceSynonymsInHighlight</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>true</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to false, words matched via synonym expansion will not be replaced by the matched synonym in the highlight results. Defaults to true.</p>

      </td>
    </tr>
    

      
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>optionalWords</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>[]</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>A string that contains the comma separated list of words that should be considered as optional when found in the query.</p>

      </td>
    </tr>
    
  
</tbody></table>

## Pagination Parameters

<table><tbody>

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>page</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer</strong></em></div><div><em>Default: <strong>0</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Pagination parameter used to select the page to retrieve.<br/>Page is zero based and defaults to 0. Thus, to retrieve the 10th page you need to set <code>page=9</code>.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>hitsPerPage</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer</strong></em></div><div><em>Default: <strong>20</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Pagination parameter used to select the number of hits per page. Defaults to 20.</p>

      </td>
    </tr>
    

</tbody></table>


## Geo-search Parameters
<table><tbody>
  

    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>aroundLatLng</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Search for entries around a given latitude/longitude (specified as two floats separated by a comma).<br/>For example, <code>aroundLatLng=47.316669,5.016670</code>.</p>

<p>By default the maximum distance is automatically guessed based on the density of the area but you can specify it manually in meters with the <strong>aroundRadius</strong> parameter. The precision for ranking can be set with <strong>aroundPrecision</strong> parameter. For example, if you set aroundPrecision=100, the distances will be considered by ranges of 100m, for example all distances 0 and 100m will be considered as identical for the &quot;geo&quot; ranking parameter.<br/><br/>When <strong>aroundRadius</strong> is not set, the radius is computed automatically using the density of the area, you can retrieve the computed radius in the <strong>automaticRadius</strong> attribute of the answer, you can also use the <strong>minimumAroundRadius</strong> query parameter to specify a minimum radius in meters for the automatic computation of <strong>aroundRadius</strong>.</p>

<p>At indexing, you should specify geoloc of an object with the _geoloc attribute (in the form <code>&quot;_geoloc&quot;:{&quot;lat&quot;:48.853409, &quot;lng&quot;:2.348800}</code> or <code>&quot;_geoloc&quot;:[{&quot;lat&quot;:48.853409, &quot;lng&quot;:2.348800},{&quot;lat&quot;:48.547456, &quot;lng&quot;:2.972075}]</code> if you have several geo-locations in your record).</p>

      </td>
    </tr>
    

  

  
    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>aroundLatLngViaIP</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Search for entries around a given latitude/longitude automatically computed from user IP address.<br/>To enable it, use <code>aroundLatLngViaIP=true</code>.</p>

<p>You can specify the maximum distance in meters with the <code>aroundRadius</code> parameter and the precision for ranking with <code>aroundPrecision</code>. For example, if you set aroundPrecision=100, two objects that are in the range 0-99m will be considered as identical in the ranking for the &quot;geo&quot; ranking parameter (same for 100-199, 200-299, ... ranges).</p>

<p>At indexing, you should specify the geo location of an object with the <code>_geoloc</code> attribute in the form <code>{&quot;_geoloc&quot;:{&quot;lat&quot;:48.853409, &quot;lng&quot;:2.348800}}</code>.</p>

      </td>
    </tr>
    

  

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>insideBoundingBox</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Search entries inside a given area defined by the two extreme points of a rectangle (defined by 4 floats: p1Lat,p1Lng,p2Lat,p2Lng).<br/>For example, <code>insideBoundingBox=47.3165,4.9665,47.3424,5.0201</code>).<br/>At indexing, you should specify geoloc of an object with the _geoloc attribute (in the form <code>&quot;_geoloc&quot;:{&quot;lat&quot;:48.853409, &quot;lng&quot;:2.348800}</code> or <code>&quot;_geoloc&quot;:[{&quot;lat&quot;:48.853409, &quot;lng&quot;:2.348800},{&quot;lat&quot;:48.547456, &quot;lng&quot;:2.972075}]</code> if you have several geo-locations in your record). You can use several bounding boxes (OR) by passing more than 4 values. For example instead of having 4 values you can pass 8 to search inside the UNION of two bounding boxes.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>insidePolygon</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Search entries inside a given area defined by a set of points (defined by a minimum of 6 floats: p1Lat,p1Lng,p2Lat,p2Lng,p3Lat,p3Long).<br/>For example <code>InsidePolygon=47.3165,4.9665,47.3424,5.0201,47.32,4.98</code>).<br/>At indexing, you should specify geoloc of an object with the _geoloc attribute (in the form <code>&quot;_geoloc&quot;:{&quot;lat&quot;:48.853409, &quot;lng&quot;:2.348800}</code> or <code>&quot;_geoloc&quot;:[{&quot;lat&quot;:48.853409, &quot;lng&quot;:2.348800},{&quot;lat&quot;:48.547456, &quot;lng&quot;:2.972075}]</code> if you have several geo-locations in your record).</p>

      </td>
    </tr>
    

</tbody></table>


## Parameters to Control Results Content

<table><tbody>
  
    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesToRetrieve</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>A string that contains the list of attributes you want to retrieve in order to minimize the size of the JSON answer.</p>

<p>Attributes are separated with a comma (for example <code>&quot;name,address&quot;</code>). You can also use a string array encoding (for example <code>[&quot;name&quot;,&quot;address&quot;]</code> ). By default, all attributes are retrieved. You can also use <code>*</code> to retrieve all values when an <strong>attributesToRetrieve</strong> setting is specified for your index.</p>

<p><code>objectID</code> is always retrieved even when not specified.</p>

      </td>
    </tr>
    

    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesToHighlight</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>A string that contains the list of attributes you want to highlight according to the query. Attributes are separated by commas. You can also use a string array encoding (for example <code>[&quot;name&quot;,&quot;address&quot;]</code>). If an attribute has no match for the query, the raw value is returned. By default, all indexed attributes are highlighted. You can use <code>*</code> if you want to highlight all attributes. A matchLevel is returned for each highlighted attribute and can contain:</p>

<ul>
<li><strong>full</strong>: If all the query terms were found in the attribute.</li>
<li><strong>partial</strong>: If only some of the query terms were found.</li>
<li><strong>none</strong>: If none of the query terms were found.</li>
</ul>

      </td>
    </tr>
    

    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesToSnippet</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>A string that contains the list of attributes to snippet alongside the number of words to return (syntax is <code>attributeName:nbWords</code>). Attributes are separated by commas (Example: <code>attributesToSnippet=name:10,content:10</code>).</p>

<p>You can also use a string array encoding (Example: <code>attributesToSnippet: [&quot;name:10&quot;,&quot;content:10&quot;]</code>). By default, no snippet is computed.</p>

      </td>
    </tr>
    

    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>getRankingInfo</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to 1, the result hits will contain ranking information in the <code>_rankingInfo</code> attribute.</p>

      </td>
    </tr>
    

    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>highlightPreTag</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div><div><em>Default: <strong>&lt;em&gt;</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the string that is inserted before the highlighted parts in the query result (defaults to <code>&lt;em&gt;</code>).</p>

      </td>
    </tr>
    

    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>highlightPostTag</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div><div><em>Default: <strong>&lt;/em&gt;</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the string that is inserted after the highlighted parts in the query result (defaults to <code>&lt;/em&gt;</code>)</p>

      </td>
    </tr>
    

    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>snippetEllipsisText</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div><div><em>Default: <strong>''</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>String used as an ellipsis indicator when a snippet is truncated. Defaults to an empty string for all accounts created before 10/2/2016, and to <code>…</code> (UTF-8 U+2026) for accounts created after that date.</p>

      </td>
    </tr>
    




  

</tbody></table>

## Numeric Search Parameters

<table><tbody>
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>numericFilters</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>A string that contains the comma separated list of numeric filters you want to apply. The filter syntax is <code>attributeName</code> followed by <code>operand</code> followed by <code>value</code>. Supported operands are <code>&lt;</code>, <code>&lt;=</code>, <code>=</code>, <code>&gt;</code> and <code>&gt;=</code>.</p>

      </td>
    </tr>
    
</tbody></table>

You can easily perform range queries via the `:` operator. This is equivalent to combining a `>=` and `<=` operand. For example, `numericFilters=price:10 to 1000`.

You can also mix OR and AND operators. The OR operator is defined with a parenthesis syntax. For example, `(code=1 AND (price:[0-100] OR price:[1000-2000]))` translates to `encodeURIComponent("code=1,(price:0 to 100,price:1000 to 2000)")`.

You can also use a string array encoding (for example `numericFilters: ["price>100","price<1000"]`).

## Category Search Parameters

<table><tbody>
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>tagFilters</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Filter the query by a set of tags. You can AND tags by separating them with commas. To OR tags, you must add parentheses. For example, <code>tags=tag1,(tag2,tag3)</code> means <em>tag1 AND (tag2 OR tag3)</em>. You can also use a string array encoding. For example, <code>tagFilters: [&quot;tag1&quot;,[&quot;tag2&quot;,&quot;tag3&quot;]]</code> means <em>tag1 AND (tag2 OR tag3)</em>.</p>

<p>At indexing, tags should be added in the <strong>_tags</strong> attribute of objects. For example <code>{&quot;_tags&quot;:[&quot;tag1&quot;,&quot;tag2&quot;]}</code>.</p>

      </td>
    </tr>
    
</tbody></table>

## Faceting Parameters

<table><tbody>

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>facetFilters</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Filter the query with a list of facets. Facets are separated by commas and is encoded as <code>attributeName:value</code>. To OR facets, you must add parentheses. For example: <code>facetFilters=(category:Book,category:Movie),author:John%20Doe</code>. You can also use a string array encoding. For example, <code>[[&quot;category:Book&quot;,&quot;category:Movie&quot;],&quot;author:John%20Doe&quot;]</code>.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>facets</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>List of object attributes that you want to use for faceting. For each of the declared attributes, you&#39;ll be able to retrieve a list of the most relevant facet values, and their associated count for the current query.</p>

<p>Attributes are separated by a comma. For example, <code>&quot;category,author&quot;</code>. You can also use JSON string array encoding. For example, <code>[&quot;category&quot;,&quot;author&quot;]</code>. Only the attributes that have been added in <strong>attributesForFaceting</strong> index setting can be used in this parameter. You can also use <code>*</code> to perform faceting on all attributes specified in <code>attributesForFaceting</code>. If the number of results is important, the count can be approximate, the attribute <code>exhaustiveFacetsCount</code> in the response is true when the count is exact.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>maxValuesPerFacet</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Limit the number of facet values returned for each facet. For example, <code>maxValuesPerFacet=10</code> will retrieve a maximum of 10 values per facet.</p>

      </td>
    </tr>
    

</tbody></table>

## Unified Filter Parameter (SQL - like)

<table><tbody>

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>filters</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Filter the query with numeric, facet or/and tag filters. The syntax is a SQL like syntax, you can use the OR and AND keywords. The syntax for the underlying numeric, facet and tag filters is the same than in the other filters:
<code>available=1 AND (category:Book OR NOT category:Ebook) AND public</code>
<code>date: 1441745506 TO 1441755506 AND inStock &gt; 0 AND author:&quot;John Doe&quot;</code></p>

<p>The list of keywords is:</p>

<ul>
<li><code>OR</code>: create a disjunctive filter between two filters.</li>
<li><code>AND</code>: create a conjunctive filter between two filters.</li>
<li><code>TO</code>: used to specify a range for a numeric filter.</li>
<li><code>NOT</code>: used to negate a filter. The syntax with the <code>-</code> isn’t allowed.</li>
</ul>

      </td>
    </tr>
    
</tbody></table>
*Note*: To specify a value with spaces or with a value equal to a keyword, it's possible to add quotes.

**Warning:**

* Like for the other filters (for performance reasons), it's not possible to have FILTER1 OR (FILTER2 AND FILTER3).
* It's not possible to mix different categories of filters inside an OR like: num=3 OR tag1 OR facet:value
* It's not possible to negate a group, it's only possible to negate a filter:  NOT(FILTER1 OR (FILTER2) is not allowed.


## Distinct Parameter

<table><tbody>

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>distinct</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to 1, enables the distinct feature, disabled by default, if the <code>attributeForDistinct</code> index setting is set. This feature is similar to the SQL &quot;distinct&quot; keyword. When enabled in a query with the <code>distinct=1</code> parameter, all hits containing a duplicate value for the attributeForDistinct attribute are removed from results. For example, if the chosen attribute is <code>show_name</code> and several hits have the same value for <code>show_name</code>, then only the best one is kept and the others are removed.</p>

      </td>
    </tr>
    

</tbody></table>

To get a full understanding of how `Distinct` works, you can have a look at our [guide on distinct](https://www.algolia.com/doc/search/distinct).





Multiple queries
==================

You can send multiple queries with a single API call using a batch of queries:

```scala
// perform 3 queries in a single API call:
//  - 1st query targets index `categories`
//  - 2nd and 3rd queries target index `products`

val result: Future[MultiQueriesResult] = client.execute {
	multiQueries(
		search into "categories" query Query(query = myQueryString, hitsPerPage = Some(3)),
		search into "products" query Query(query = myQueryString, hitsPerPage = Some(3), tagFilters = Some(Seq("promotion"))),
		search into "products" query Query(query = myQueryString, hitsPerPage = Some(10))
	) strategy MultiQueries.Strategy.stopIfEnoughMatches
}
```

The resulting JSON answer contains a ```results``` array storing the underlying queries answers. The answers order is the same than the requests order.

You can specify a `strategy` parameter to optimize your multiple queries:
- `none`: Execute the sequence of queries until the end.
- `stopIfEnoughMatches`: Execute the sequence of queries until the number of hits is reached by the sum of hits.



Get an object
==================

You can easily retrieve an object using its `objectID` and optionally specify a comma separated list of attributes you want:

```scala
// Retrieves all attributes
client.execute {
	get objectId "myId" from "index"
}

//or
client.execute {
	get from "index" objectId "myId"
}
```

You can get a case object by:
```scala
val result = client.execute {
	get / "index" / "myId"
}

result.map(_.as[Contact])
```

You can also retrieve a set of objects:

```scala
client.execute {
	get from "index" objectIds Seq("myId", "myOtherId")
}
```

You can get a case object by:
```scala
val result = client.execute {
	get from "index" objectIds Seq("myId", "myOtherId")
}

result.map(_.as[Contact])
```

Delete an object
==================

You can delete an object using its `objectID`:

```scala
client.execute { delete from "toto" objectId "oid" }
```



Index Settings
==================

You can easily retrieve or update settings:

```scala
val result: Future[IndexSettings] = client.execute {
  settings of "myIndex"
}
```

```scala
val result: Future[Task] = client.execute {
  changeSettings of "myIndex" `with` IndexSettings(
    attributesToIndex = Some(Seq(AttributesToIndex.attribute("att1"), AttributesToIndex.attributes("att2", "att3"), AttributesToIndex.unordered("att4"))),
    numericAttributesToIndex = Some(Seq(NumericAttributesToIndex.equalOnly("att5"))),
    ranking = Some(Seq(
      Ranking.typo,
      Ranking.geo,
      Ranking.words,
      Ranking.proximity,
      Ranking.attribute,
      Ranking.exact,
      Ranking.custom,
      Ranking.asc("att6"),
      Ranking.desc("att7")
    )),
    customRanking = Some(Seq(
      CustomRanking.asc("att8"),
      CustomRanking.desc("att9")
    )),
    synonyms = Some(Seq(Seq("black", "dark"), Seq("small", "little", "mini"))),
    placeholders = Some(Map("<streetnumber>" -> Seq("1", "2", "3", "4", "5"))),
    altCorrections = Some(Seq(AltCorrection("foot", "feet", 1))),
    typoTolerance = Some(TypoTolerance.strict)
  )
}
```


## Indexing parameters

<table><tbody>

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesToIndex</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The list of attributes you want index (i.e. to make searchable).</p>

<p>If set to null, all textual and numerical attributes of your objects are indexed. Make sure you updated this setting to get optimal results.</p>

<p>This parameter has two important uses:</p>

<ul>
<li><em>Limit the attributes to index</em>.<br/>For example, if you store the URL of a picture, you want to store it and be able to retrieve it, but you probably don&#39;t want to search in the URL.</li>
<li><em>Control part of the ranking</em>.<br/> Matches in attributes at the beginning of the list will be considered more important than matches in attributes further down the list. In one attribute, matching text at the beginning of the attribute will be considered more important than text after. You can disable this behavior if you add your attribute inside <code>unordered(AttributeName)</code>. For example, <code>attributesToIndex: [&quot;title&quot;, &quot;unordered(text)&quot;]</code>.
You can decide to have the same priority for two attributes by passing them in the same string using a comma as a separator. For example <code>title</code> and <code>alternative_title</code> have the same priority in this example, which is different than text priority: <code>attributesToIndex:[&quot;title,alternative_title&quot;, &quot;text&quot;]</code>.
To get a full description of how the Ranking works, you can have a look at our <a href="https://www.algolia.com/doc/relevance/ranking">Ranking guide</a>.</li>
<li><strong>numericAttributesToIndex</strong>: (array of strings) All numerical attributes are automatically indexed as numerical filters (allowing filtering operations like <code>&lt;</code> and <code>&lt;=</code>). If you don&#39;t need filtering on some of your numerical attributes, you can specify this list to speed up the indexing.<br/> If you only need to filter on a numeric value with the operator &#39;=&#39;, you can speed up the indexing by specifying the attribute with <code>equalOnly(AttributeName)</code>. The other operators will be disabled.</li>
</ul>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesForFaceting</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The list of fields you want to use for faceting. All strings in the attribute selected for faceting are extracted and added as a facet. If set to null, no attribute is used for faceting.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributeForDistinct</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The name of the attribute used for the <code>Distinct</code> feature. This feature is similar to the SQL &quot;distinct&quot; keyword. When enabled in queries with the <code>distinct=1</code> parameter, all hits containing a duplicate value for this attribute are removed from the results. For example, if the chosen attribute is <code>show_name</code> and several hits have the same value for <code>show_name</code>, then only the first one is kept and the others are removed from the results. To get a full understanding of how <code>Distinct</code> works, you can have a look at our <a href="https://www.algolia.com/doc/search/distinct">guide on distinct</a>.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>ranking</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Controls the way results are sorted.</p>

<p>We have nine available criteria:</p>

<ul>
<li><code>typo</code>: Sort according to number of typos.</li>
<li><code>geo</code>: Sort according to decreasing distance when performing a geo location based search.</li>
<li><code>words</code>: Sort according to the number of query words matched by decreasing order. This parameter is useful when you use the <code>optionalWords</code> query parameter to have results with the most matched words first.</li>
<li><code>proximity</code>: Sort according to the proximity of the query words in hits.</li>
<li><code>attribute</code>: Sort according to the order of attributes defined by attributesToIndex.</li>
<li><code>exact</code>:

<ul>
<li>If the user query contains one word: sort objects having an attribute that is exactly the query word before others. For example, if you search for the TV show &quot;V&quot;, you want to find it with the &quot;V&quot; query and avoid getting all popular TV shows starting by the letter V before it.</li>
<li>If the user query contains multiple words: sort according to the number of words that matched exactly (not as a prefix).</li>
</ul></li>
<li><code>custom</code>: Sort according to a user defined formula set in the <code>customRanking</code> attribute.</li>
<li><code>asc(attributeName)</code>: Sort according to a numeric attribute using ascending order. <code>attributeName</code> can be the name of any numeric attribute in your records (integer, double or boolean).</li>
<li><code>desc(attributeName)</code>: Sort according to a numeric attribute using descending order. <code>attributeName</code> can be the name of any numeric attribute in your records (integer, double or boolean). <br/>The standard order is <code>[&quot;typo&quot;, &quot;geo&quot;, &quot;words&quot;, &quot;proximity&quot;, &quot;attribute&quot;, &quot;exact&quot;, &quot;custom&quot;]</code>.
To get a full description of how the Ranking works, you can have a look at our <a href="https://www.algolia.com/doc/relevance/ranking">Ranking guide</a>.</li>
</ul>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>customRanking</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Lets you specify part of the ranking.</p>

<p>The syntax of this condition is an array of strings containing attributes prefixed by the asc (ascending order) or desc (descending order) operator. For example, <code>&quot;customRanking&quot; =&gt; [&quot;desc(population)&quot;, &quot;asc(name)&quot;]</code>.</p>

<p>To get a full description of how the Custom Ranking works, you can have a look at our <a href="https://www.algolia.com/doc/relevance/ranking">Ranking guide</a>.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>queryType</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>prefixLast</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Select how the query words are interpreted. It can be one of the following values:</p>

<ul>
<li><code>prefixAll</code>: All query words are interpreted as prefixes.</li>
<li><code>prefixLast</code>: Only the last word is interpreted as a prefix (default behavior).</li>
<li><code>prefixNone</code>: No query word is interpreted as a prefix. This option is not recommended.</li>
</ul>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>separatorsToIndex</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>empty</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the separators (punctuation characters) to index. By default, separators are not indexed. Use <code>+#</code> to be able to search Google+ or C#.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>slaves</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The list of indices on which you want to replicate all write operations. In order to get response times in milliseconds, we pre-compute part of the ranking during indexing. If you want to use different ranking configurations depending of the use case, you need to create one index per ranking configuration. This option enables you to perform write operations only on this index and automatically update slave indices with the same operations.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>unretrievableAttributes</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>empty</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The list of attributes that cannot be retrieved at query time. This feature allows you to have attributes that are used for indexing and/or ranking but cannot be retrieved. Defaults to null. Warning: for testing purposes, this setting is ignored when you&#39;re using the ADMIN API Key.</p>

      </td>
    </tr>
    

  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>allowCompressionOfIntegerArray</code></div>
            <div class="client-readme-param-meta"><div><em>Default: <strong>false</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Allows compression of big integer arrays. In data-intensive use-cases, we recommended enabling this feature and then storing the list of user IDs or rights as an integer array. When enabled, the integer array is reordered to reach a better compression ratio. Defaults to false.</p>

      </td>
    </tr>
    

</tbody></table>

## Query expansion

<table><tbody>
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>synonyms</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of array of string considered as equals</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>For example, you may want to retrieve the <strong>black ipad</strong> record when your users are searching for <strong>dark ipad</strong>, even if the word <strong>dark</strong> is not part of the record. To do this, you need to configure <strong>black</strong> as a synonym of <strong>dark</strong>. For example, <code>&quot;synomyms&quot;: [ [ &quot;black&quot;, &quot;dark&quot; ], [ &quot;small&quot;, &quot;little&quot;, &quot;mini&quot; ], ... ]</code>. The Synonym feature also supports multi-words expressions like <code>&quot;synonyms&quot;: [ [&quot;NYC&quot;, &quot;New York City&quot;] ]</code></p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>placeholders</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>hash of array of words</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>This is an advanced use-case to define a token substitutable by a list of words without having the original token searchable. It is defined by a hash associating placeholders to lists of substitutable words. For example, <code>&quot;placeholders&quot;: { &quot;&lt;streetnumber&gt;&quot;: [&quot;1&quot;, &quot;2&quot;, &quot;3&quot;, ..., &quot;9999&quot;]}</code> would allow it to be able to match all street numbers. We use the <code>&lt; &gt;</code> tag syntax to define placeholders in an attribute. For example:</p>

<ul>
<li>Push a record with the placeholder: <code>{ &quot;name&quot; : &quot;Apple Store&quot;, &quot;address&quot; : &quot;&amp;lt;streetnumber&amp;gt; Opera street, Paris&quot; }</code>.</li>
<li>Configure the placeholder in your index settings: <code>&quot;placeholders&quot;: { &quot;&lt;streetnumber&gt;&quot; : [&quot;1&quot;, &quot;2&quot;, &quot;3&quot;, &quot;4&quot;, &quot;5&quot;, ... ], ... }</code>.</li>
</ul>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>disableTypoToleranceOnWords</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string array</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify a list of words on which automatic typo tolerance will be disabled.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>disableTypoToleranceOnAttributes</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string array</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>List of attributes on which you want to disable typo tolerance (must be a subset of the <code>attributesToIndex</code> index setting). By default the list is empty.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>altCorrections</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>object array</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify alternative corrections that you want to consider. Each alternative correction is described by an object containing three attributes:</p>

<ul>
<li><strong>word</strong>: The word to correct.</li>
<li><strong>correction</strong>: The corrected word.</li>
<li><strong>nbTypos</strong> The number of typos (1 or 2) that will be considered for the ranking algorithm (1 typo is better than 2 typos).</li>
</ul>

<p>For example <code>&quot;altCorrections&quot;: [ { &quot;word&quot; : &quot;foot&quot;, &quot;correction&quot;: &quot;feet&quot;, &quot;nbTypos&quot;: 1 }, { &quot;word&quot;: &quot;feet&quot;, &quot;correction&quot;: &quot;foot&quot;, &quot;nbTypos&quot;: 1 } ]</code>.</p>

      </td>
    </tr>
    

</tbody></table>

## Default query parameters (can be overwritten by queries)

<table><tbody>
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>minWordSizefor1Typo</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer</strong></em></div><div><em>Default: <strong>4</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The minimum number of characters needed to accept one typo (default = 4).</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>minWordSizefor2Typos</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer</strong></em></div><div><em>Default: <strong>8</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The minimum number of characters needed to accept two typos (default = 8).</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>hitsPerPage</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer</strong></em></div><div><em>Default: <strong>10</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>The number of hits per page (default = 10).</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesToRetrieve</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Default list of attributes to retrieve in objects. If set to null, all attributes are retrieved.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesToHighlight</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Default list of attributes to highlight. If set to null, all indexed attributes are highlighted.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>attributesToSnippet</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Default list of attributes to snippet alongside the number of words to return (syntax is <code>attributeName:nbWords</code>).<br/>By default, no snippet is computed. If set to null, no snippet is computed.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>highlightPreTag</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the string that is inserted before the highlighted parts in the query result (defaults to <code>&lt;em&gt;</code>).</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>highlightPostTag</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the string that is inserted after the highlighted parts in the query result (defaults to <code>&lt;/em&gt;</code>).</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>optionalWords</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>array of strings</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify a list of words that should be considered optional when found in the query.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>allowTyposOnNumericTokens</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>boolean</strong></em></div><div><em>Default: <strong>false</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to false, disable typo-tolerance on numeric tokens (=numbers) in the query word. For example the query <code>&quot;304&quot;</code> will match with <code>&quot;30450&quot;</code>, but not with <code>&quot;40450&quot;</code> that would have been the case with typo-tolerance enabled. Can be very useful on serial numbers and zip codes searches. Defaults to false.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>ignorePlurals</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>boolean</strong></em></div><div><em>Default: <strong>false</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to true, singular/plural forms won’t be considered as typos (for example car/cars and foot/feet will be considered as equivalent). Defaults to false.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>advancedSyntax</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer (0 or 1)</strong></em></div><div><em>Default: <strong>0</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Enable the advanced query syntax. Defaults to 0 (false).</p>

<ul>
<li><p><strong>Phrase query:</strong> a phrase query defines a particular sequence of terms. A phrase query is build by Algolia&#39;s query parser for words surrounded by <code>&quot;</code>. For example, <code>&quot;search engine&quot;</code> will retrieve records having <code>search</code> next to <code>engine</code> only. Typo-tolerance is disabled on phrase queries.</p></li>
<li><p><strong>Prohibit operator:</strong> The prohibit operator excludes records that contain the term after the <code>-</code> symbol. For example <code>search -engine</code> will retrieve records containing <code>search</code> but not <code>engine</code>.</p></li>
</ul>

      </td>
    </tr>
    
    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>replaceSynonymsInHighlight</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>boolean</strong></em></div><div><em>Default: <strong>true</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>If set to false, words matched via synonyms expansion will not be replaced by the matched synonym in the highlighted result. Defaults to true.</p>

      </td>
    </tr>
    
    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>maxValuesPerFacet</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Limit the number of facet values returned for each facet. For example: <code>maxValuesPerFacet=10</code> will retrieve max 10 values per facet.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>distinct</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>integer (0 or 1)</strong></em></div><div><em>Default: <strong>0</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Enable the distinct feature (disabled by default) if the <code>attributeForDistinct</code> index setting is set. This feature is similar to the SQL &quot;distinct&quot; keyword: when enabled in a query with the <code>distinct=1</code> parameter, all hits containing a duplicate value for the<code>attributeForDistinct</code> attribute are removed from results. For example, if the chosen attribute is <code>show_name</code> and several hits have the same value for <code>show_name</code>, then only the best one is kept and others are removed.</p>

<p>To get a full understanding of how <code>Distinct</code> works, you can have a look at our <a href="https://www.algolia.com/doc/search/distinct">guide on distinct</a>.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>typoTolerance</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>string</strong></em></div><div><em>Default: <strong>true</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>This setting has four different options:</p>

<ul>
<li><p><code>true:</code> activate the typo-tolerance (default value).</p></li>
<li><p><code>false:</code> disable the typo-tolerance</p></li>
<li><p><code>min:</code> keep only results with the lowest number of typos. For example if one result matches without typos, then all results with typos will be hidden.</p></li>
<li><p><code>strict:</code> if there is a match without typo, then all results with 2 typos or more will be removed.</p></li>
</ul>

      </td>
    </tr>
    
    
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>removeStopWords</code></div>
            <div class="client-readme-param-meta"><div><em>Type: <strong>boolean</strong></em></div><div><em>Default: <strong>false</strong></em></div></div>
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Remove stop words from query before executing it. Defaults to false. Contains stop words for 41 languages (Arabic, Armenian, Basque, Bengali, Brazilian, Bulgarian, Catalan, Chinese, Czech, Danish, Dutch, English, Finnish, French, Galician, German, Greek, Hindi, Hungarian, Indonesian, Irish, Italian, Japanese, Korean, Kurdish, Latvian, Lithuanian, Marathi, Norwegian, Persian, Polish, Portugese, Romanian, Russian, Slovak, Spanish, Swedish, Thai, Turkish, Ukranian, Urdu)</p>

      </td>
    </tr>
    
  </tbody></table>




List indices
==================
You can list all your indices along with their associated information (number of entries, disk size, etc.) with the `` method:

```scala
val indices: Future[Indices] = client.execute { list.indices }
```





Delete an index
==================
You can delete an index using its name:

```scala
client.execute { delete index "index" }
```





Clear an index
==================
You can delete the index contents without removing settings and index specific API keys by using the clearIndex command:

```scala
client.execute { clear index "index" }
```

Wait indexing
==================

All write operations in Algolia are asynchronous by design.

It means that when you add or update an object to your index, our servers will
reply to your request with a `taskID` as soon as they understood the write
operation.

The actual insert and indexing will be done after replying to your code.

You can wait for a task to complete using the `waitTask` method on the `taskID` returned by a write operation.

For example, to wait for indexing of a new object:
```scala
for {
  t <- client.execute { index into "toto" `object` MyObject("test") }
  r <- client.execute { waitFor task t from "toto" }
} yield "indexing is done"
```

If you want to ensure multiple objects have been indexed, you only need to check
the biggest `taskID`.

Batch writes
==================

You may want to perform multiple operations with one API call to reduce latency.
We expose four methods to perform batch operations:
 * ``: Add an array of objects using automatic `objectID` assignment.
 * ``: Add or update an array of objects that contains an `objectID` attribute.
 * ``: Delete an array of objectIDs.
 * ``: Partially update an array of objects that contain an `objectID` attribute (only specified attributes will be updated).

Example using automatic `objectID` assignment:
```scala
client.execute {
	batch(
  	index into "index1" `object` Contact("Jimmie", "Barninger")
  	index into "index1" `object` Contact("Warren", "Speach")
	)
}

//or

client.execute {
	index into "index1" `object` Seq(Contact("Jimmie", "Barninger"), Contact("Warren", "Speach"))
}
```

/!\ This does not (yet)[https://github.com/algolia/algoliasearch-client-scala/issues/32] work. /!\
```scala
client.execute {
	batch(
  	index into "test1" objects Seq(Contact("Jimmie", "Barninger"), Contact("Warren", "Speach"))
	)
}
```

Example with user defined `objectID` (add or update):
```scala
client.execute {
	batch(
    update attribute "firstname" value "Jimmie" ofObjectId "SFO" from "index",
    update attribute "lastname" value "Barninger" ofObjectId "SFO" from "index",
    update attribute "firstname" value "Warren" ofObjectId "LA" from "index",
    update attribute "lastname" value "Speach" ofObjectId "LA" from "index"
  )
}
```

Example that deletes a set of records:
```scala
client.execute {
	batch(
  	delete from "test1" objectId "1",
  	delete from "test2" objectId "2"
	)
}
```

Example that updates only the `firstname` attribute:
```scala
client.execute {
  update attribute "firstname" value "Jimmie" ofObjectId "SFO" from "index",
}
```



If you have one index per user, you may want to perform a batch operations across severals indexes.
We expose a method to perform this type of batch:
```scala
client.execute {
	batch(
  	index into "index1" `object` Contact("Jimmie", "Barninger")
  	index into "index2" `object` Contact("Warren", "Speach")
	)
}
```

The attribute **action** can have these values:
- addObject
- updateObject
- partialUpdateObject
- partialUpdateObjectNoCreate
- deleteObject

Copy / Move an index
==================

You can easily copy or rename an existing index using the `copy` and `move` commands.
**Note**: Move and copy commands overwrite the destination index.

```scala
// Rename MyIndex in MyIndexNewName
client.execute { move index "MyIndex" to "MyIndexNewName" }

// Copy MyIndex in MyIndexCopy
client.execute { copy index "MyIndex" to "MyIndexNewName" }
```

The move command is particularly useful if you want to update a big index atomically from one version to another. For example, if you recreate your index `MyIndex` each night from a database by batch, you only need to:
 1. Import your database into a new index using [batches](#batch-writes). Let's call this new index `MyNewIndex`.
 1. Rename `MyNewIndex` to `MyIndex` using the move command. This will automatically override the old index and new queries will be served on the new one.

```scala
// Rename MyNewIndex in MyIndex (and overwrite it)
client.execute { move index "MyIndex" to "MyIndexNewName" }
```

Backup / Export an index
==================

The `search` method cannot return more than 1,000 results. If you need to
retrieve all the content of your index (for backup, SEO purposes or for running
a script on it), you should use the `browse` method instead. This method lets
you retrieve objects beyond the 1,000 limit.

This method is optimized for speed. To make it fast, distinct, typo-tolerance,
word proximity, geo distance and number of matched words are disabled. Results
are still returned ranked by attributes and custom ranking.


It will return a `cursor` alongside your data, that you can then use to retrieve
the next chunk of your records.

You can specify custom parameters (like `page` or `hitsPerPage`) on your first
`browse` call, and these parameters will then be included in the `cursor`. Note
that it is not possible to access records beyond the 1,000th on the first call.

Example:

```scala
val q = Query(query = Some("text"), numericFilters = Some("i<42"))

// Iterate with a filter over the index
val result: Future[BrowseResult] = client.execute {
	browse index "myIndex" query q
}

result
	.map(_.cursor) // Retrieve the next cursor
	.flatMap { cursor =>
		client.execute {
			//continue the browse with the cursor
			browse index "myIndex" from cursor
		}
	}
```





API Keys
==================

The **admin** API key provides full control of all your indices. *The admin API key should always be kept secure; do NOT use it from outside your back-end.*

You can also generate user API keys to control security.
These API keys can be restricted to a set of operations or/and restricted to a given index.

## List API keys

To list existing keys, you can use:

```scala
//global
client.execute {
	get allKeys
}

//index
client.execute {
	get allKeysFrom "myIndex"
}
```

Each key is defined by a set of permissions that specify the authorized actions. The different permissions are:
 * **search**: Allowed to search.
 * **browse**: Allowed to retrieve all index contents via the browse API.
 * **addObject**: Allowed to add/update an object in the index.
 * **deleteObject**: Allowed to delete an existing object.
 * **deleteIndex**: Allowed to delete index content.
 * **settings**: allows to get index settings.
 * **editSettings**: Allowed to change index settings.
 * **analytics**: Allowed to retrieve analytics through the analytics API.
 * **listIndexes**: Allowed to list all accessible indexes.

## Create API keys

To create API keys:

```scala
// Creates a new global API key that can only perform search actions
val apiKey = ApiKey(
	acl = Some(Seq(Acl.search)),
)

//global
client.execute {
	add key apiKey
}

//for an index
client.execute {
	add key apiKey to "myIndex"
}
```

You can also create an API Key with advanced settings:

<table><tbody>
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>validity</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Add a validity period. The key will be valid for a specific period of time (in seconds).</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>maxQueriesPerIPPerHour</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the maximum number of API calls allowed from an IP address per hour. Each time an API call is performed with this key, a check is performed. If the IP at the source of the call did more than this number of calls in the last hour, a 403 code is returned. Defaults to 0 (no rate limit). This parameter can be used to protect you from attempts at retrieving your entire index contents by massively querying the index.</p>

<p>Note: If you are sending the query through your servers, you must use the <code>enableRateLimitForward(&quot;TheAdminAPIKey&quot;, &quot;EndUserIP&quot;, &quot;APIKeyWithRateLimit&quot;)</code> function to enable rate-limit.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>maxHitsPerQuery</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the maximum number of hits this API key can retrieve in one call. Defaults to 0 (unlimited). This parameter can be used to protect you from attempts at retrieving your entire index contents by massively querying the index.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>indexes</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the list of targeted indices. You can target all indices starting with a prefix or ending with a suffix using the &#39;*&#39; character. For example, &quot;dev_*&quot; matches all indices starting with &quot;dev_&quot; and &quot;*_dev&quot; matches all indices ending with &quot;_dev&quot;. Defaults to all indices if empty or blank.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>referers</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the list of referers. You can target all referers starting with a prefix or ending with a suffix using the &#39;*&#39; character. For example, &quot;algolia.com/*&quot; matches all referers starting with &quot;algolia.com/&quot; and &quot;*.algolia.com&quot; matches all referers ending with &quot;.algolia.com&quot;. Defaults to all referers if empty or blank.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>queryParameters</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the list of query parameters. You can force the query parameters for a query using the url string format (param1=X&amp;param2=Y...).</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>description</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify a description to describe where the key is used.</p>

      </td>
    </tr>
    

</tbody></table>

```scala
// Creates a new global API key that is valid for 300 seconds
val apiKey = ApiKey(
	acl = Some(Seq(Acl.search)),
	maxHitsPerQuery = Some(20)),
	maxQueriesPerIPPerHour = Some(100),
	validity = Some(300),
	indexes = Some(Seq("myIndex")),
	referers = Some(Seq("algolia.com/*")),
	queryParameters = Some(Seq("typoTolerance=strict&ignorePlurals=false")),
	description = Some("Limited search only API key for algolia.com")
)

//global
client.execute {
	add key apiKey
}

//for an index
client.execute {
	add key apiKey to "myIndex"
}
```

## Update API keys

To update the permissions of an existing key:

```scala
// Creates a new global API key that is valid for 300 seconds
client.execute {
	update key "myAPIKey" `with` ApiKey(
		acl = Some(Seq(Acl.search)),
		maxHitsPerQuery = Some(0)),
		maxQueriesPerIPPerHour = Some(0),
		validity = Some(300)
	)
}

// Update a index specific API key valid for 300 seconds, with a rate limit of 100 calls per hour per IP and a
val apiKey = ApiKey(
	acl = Some(Seq(Acl.search)),
	maxHitsPerQuery = Some(100)),
	maxQueriesPerIPPerHour = Some(100),
	validity = Some(300)
)

client.execute {
	update key "myAPIKey" `with` apiKey from "myIndex"
}
```
To get the permissions of a given key:
```scala
//global
client.execute {
	get key "f420238212c54dcfad07ea0aa6d5c45f"
}

//index
client.execute {
	get key "f420238212c54dcfad07ea0aa6d5c45f" from "myIndex"
}
```

## Delete API keys

To delete an existing key:
```scala
//global
client.execute {
	delete key "f420238212c54dcfad07ea0aa6d5c45f"
}

//for an index
client.execute {
	delete key "f420238212c54dcfad07ea0aa6d5c45f" from "myIndex"
}
```



## Secured API keys (frontend)

You may have a single index containing **per user** data. In that case, all records should be tagged with their associated `user_id` in order to add a `tagFilters=user_42` filter at query time to retrieve only what a user has access to. If you're using the [JavaScript client](http://github.com/algolia/algoliasearch-client-js), it will result in a security breach since the user is able to modify the `tagFilters` you've set by modifying the code from the browser. To keep using the JavaScript client (recommended for optimal latency) and target secured records, you can generate a secured API key from your backend:

```scala
// generate a public API key for user 42. Here, records are tagged with:
//  - 'user_XXXX' if they are visible by user XXXX
String publicKey = client.generateSecuredApiKey("YourSearchOnlyApiKey", Query(tagFilters = Some(Seq("user_42"))))
```

This public API key can then be used in your JavaScript code as follow:

```js
var client = algoliasearch('YourApplicationID', '<%= public_api_key %>');

var index = client.initIndex('indexName')

index.search('something', function(err, content) {
  if (err) {
    console.error(err);
    return;
  }

  console.log(content);
});
```

You can mix rate limits and secured API keys by setting a `userToken` query parameter at API key generation time. When set, a unique user will be identified by her `IP + user_token` instead of only by her `IP`. This allows you to restrict a single user to performing a maximum of `N` API calls per hour, even if she shares her `IP` with another user.

```scala
// generate a public API key for user 42. Here, records are tagged with:
//  - 'user_XXXX' if they are visible by user XXXX
String publicKey = client.generateSecuredApiKey("YourSearchOnlyApiKey", Query(tagFilters = Some(Seq("user_42"))), Some("42"))
```

This public API key can then be used in your JavaScript code as follow:

```js
var client = algoliasearch('YourApplicationID', '<%= public_api_key %>');

var index = client.initIndex('indexName')

index.search('another query', function(err, content) {
  if (err) {
    console.error(err);
    return;
  }

  console.log(content);
});
```




Logs
==================

You can retrieve the latest logs via this API. Each log entry contains:
 * Timestamp in ISO-8601 format
 * Client IP
 * Request Headers (API Key is obfuscated)
 * Request URL
 * Request method
 * Request body
 * Answer HTTP code
 * Answer body
 * SHA1 ID of entry

You can retrieve the logs of your last 1,000 API calls and browse them using the offset/length parameters:

<table><tbody>
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>offset</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the first entry to retrieve (0-based, 0 is the most recent log entry). Defaults to 0.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>length</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the maximum number of entries to retrieve starting at the offset. Defaults to 10. Maximum allowed value: 1,000.</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>onlyErrors</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Retrieve only logs with an HTTP code different than 200 or 201. (deprecated)</p>

      </td>
    </tr>
    
  
    <tr>
      <td valign='top'>
        <div class='client-readme-param-container'>
          <div class='client-readme-param-container-inner'>
            <div class='client-readme-param-name'><code>type</code></div>
            
          </div>
        </div>
      </td>
      <td class='client-readme-param-content'>
        <p>Specify the type of logs to retrieve:</p>

<ul>
<li><code>query</code>: Retrieve only the queries.</li>
<li><code>build</code>: Retrieve only the build operations.</li>
<li><code>error</code>: Retrieve only the errors (same as <code>onlyErrors</code> parameters).</li>
</ul>

      </td>
    </tr>
    
</tbody></table>

```scala
// Get last 10 log entries
client.execute {
	logs
}

// Get last 100 log entries
client.execute {
	logs lenght 100
}
```







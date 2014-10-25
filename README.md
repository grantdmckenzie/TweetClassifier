<h2>Tweet Classifier</h2>

Java-based Tweet classifier based on Mallet's Naive Bayes classifier.  Currently restricted to a limited number of categories:

* Personal Business
* Work / School
* Social / Recreation
* Other

<h3>InferClass</h3>

####Parameters:####

* t = The text content of a tweet.  Must be greater than 50 characters. 

####Output:####

A JSON Object containing the classification values based on the classes indicated above.

####Example:####

http://~/InferClass?t=I am so tired of my job, I can't wait to work at a new place where everyone is friendly

<h3>UserInferClass</h3>

####Parameters:####

* username = Twitter username
* page (optional) = Twitter feed page of content
* count (optional) = Twitter feed number of tweets per page.  Keep in mind that the actual number may be smaller if there are tweets with less than 50 characters (will not be classified)

####Output:####

A JSON Object containing the username and an array of tweet objects.  The tweet objects contain a date/time value as well as an array of classification values based on the classes indicated above.

####Example:####

http://~/UserInferClass?username=grantdmckenzie&page=2&count=13

TODO: 

* Better Classification based on more than single user manual classification.  
* Add "Shopping" as category.


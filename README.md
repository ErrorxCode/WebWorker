# WebWorker ~ Heroku worker with RESTAPI

A tiny heroku library designed for creating a restfull worker on heroku. With this, you can control,save,enque worker process itself from the web process **using API.** This library establish *full-duplex communication* between worker process and web process, so that you can use worker **as a backend of your app** for processing heavy tasks. This library uses **heroku REST API** and hence require **API key**.

*Use this library when you only want to process some data or perform background job on an input (data). This may not serve you if your worker perform jobs periodically (24/7) at some intervel of time*



## Implementation

**Note** : Use standalone jar

#### ~~For gradle~~ :-

Add it in your root build.gradle at the end of repositories:

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add the dependency

```gr
	dependencies {
	     implementation 'com.github.ErrorxCode:WebWorker:1.0.0'
	}
```

#### ~~For maven~~ :-

Declare the repo.

```markup
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

Add the dependencies

```markup
	<dependency>
	    <groupId>com.github.ErrorxCode</groupId>
	    <artifactId>WebWorker</artifactId>
	    <version>1.0.0</version>
	</dependency>
```



**Or else, you can download the [standalone jar](https://github.com/ErrorxCode/WebWorker/releases/download/test2/WebWorker.jar)**

## Usage

The usage of the library is so simple, just create 2 classes, one for server & another for worker (both with main method). For server, just call the `Server.start()` method and for worker, create your own extending `Worker` class.

**Example** (*MyServer.java*) :

```java
public class MyServer {
    public static void main(String[] args) {
        Server.start();
    }
}
```

**Example** (*MyWorker.java*) :

```
public class MyWorker extends Worker {
    
    public MyWorker(String serverURL) throws InterruptedException {
        super(serverURL);
    }

    @Override
    public void process(byte[] data) {
        // process your data here
    }

    @Override
    public void onStart() {
        // initialize your worker here
    }

    public static void main(String[] args) throws InterruptedException {
        new MyWorker("ws://your.herokuapp.com.worker");
    }
}
```

The data in `process(byte[] data)` method is the data that you have sent in the API request. If sent multiple data, each will be processed one-by-on.

**Example** (build.gradle)

```groovy
plugins {
    id 'application'
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

dependencies {
    // library dependency here
}

mainClassName = "your.package.MyServer"

jar {
    manifest {
        attributes "Main-Class": "$mainClassName"
    }

    from {
        exclude "META-INF/*"
        exclude "about.html"
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
}
```

With these two classes, build your application to a `jar` keeping **MyServer** the main class. Just execute the `jar` task to do so.



## Deploying on heroku

First of all, set the following config (enviournment variable) for your heroku app :

- API_KEY = your heroku api key
- APP_NAME = your heroku app name

Then, You can follow [this](https://devcenter.heroku.com/articles/deploying-executable-jar-files) guide to deploy a jar on heroku . However, your `Procfile` should be like this :

*Suppose our jar file name is Test.jar*

```
web: java -jar Test.jar
worker: java -cp Test.jar your.package.MyWorker
```

This will run `MyServer` for web process & `MyWorker` for worker. On the first start of your web process, the worker will be connected to the web server and will be shutdown after 60 seonds. After that, **the worker is ready to listen your orders**.

See the API reference on how to intrect with worker.



## API Reference

You can control your worker by sending API request to your web server. This library automatically creates several endpoints to intract with worker process. Each endpoints are followed by the ***base url*** which is your heroku app url.

#### Starting worker

```http
GET /start
```

This will start's the worker process. The worker will wait for 60 seconds to recieve data, after that it will start processing the data. If the worker didn't get any data, it will sleep.

#### Starting worker with data

```http
POST /start

// body is your binary data
```

This will start the worker and then process the provided data.



#### Sending data

```http
PATCH /queue
Content-type : multipart/form-data

data1=...
data2=...
data3=...
```

Send's multiple data to the worker. The data will be placed in queu until the worker is processing another data and will be processed sequencially.



#### Stopping worker

```http
GET /stop
```

Stops the worker, scalles it to 0.



All the endpoints will return `true` if succeed or `false` if not. You can check the error in your application logs.



## That's sit

This is how you can create a resful worker for your app. If this project helped you, please give this a star. If you wan't to work with me, contact me at hackerinsiderahil@gmail.com

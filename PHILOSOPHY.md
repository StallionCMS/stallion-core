


* Very opinionated framework. Not trying to be all things to all people, rather, trying to encompass a set of best practices for building web applications in 2016. For instance, all models have a primary key that is a long id generated by a global tickets generated. That said, if you want to swap something out with your own way of doing things, for instance, by using hibernate, you can do so without any problems.
* Smart caching built into the framework. 
* All accessories included. Recurring jobs (cron replacement), asynchcronous tasks, monitoring and health endpoints, exception reporting, etc, are all included out of the box.



Opinions Taken
-------------------------------------------------------------



*ORM's are good for the simple things, drop down to SQL for the complicated queries*

*Default structure for models and controllers*

Long ID's from a tickets table

*No checked exceptions*

*Static Logger* 


*Mocking services for testing is mostly pointless.* The entire idea of testing is to make sure all your code works together. If you mock out your database, you are not really testing your code how it will really work in the wild. And as long as your DB is on the same server, which is no problem in a test environment, talking to the DB is very, very fast. There is one exception -- I do mock out all calls to external services, such as sending an email or fetching a URL, since those make unittests unbearably slow. There are also particular tests where mocking is needed. But the default is no mocking.

Stallion has a boot phase in which services are initilialized. Rather than mocking out services entirely, in the boot phase, it can tell those services to start in test mode. That service can then make the smallest change necessary to make tests fast, while otherwise remaining as close to the real production mode as possible.

*Registry pattern is preferred to Constructor Injection pattern for framework services*

I prefer registry pattern for several reasons:

1. It's much easier to debug. It is very easy to read the code and see exactly how something is getting confiruged. It is very easy to step through with a debugger and see why something is getting initialized.
2. It's simpler, less magical, and it just works. The least complicated solution should always be preferred. 

It is usually claimed that the registry pattern has numerous downsides: 1) It makes mocking things out for testing harder. 2) It makes the calling class have a hard dependency on the registry code, meaning a client is not going to be able to swap out for another implementing class easliy. 3) It makes it less clear what the dependencies on any given class is. 4) Prevents you from having multiple services in the same application.

Those critiques may matter for some applications, but they do not matter for the applications for which Stallion was designed. For 1), Mocking simply isn't a problem because I think that mocks should rarely be used, and when I do, it is easy enough to configure the registry to load a different implemenation class in test mode. For 2) YAGNI applies. If you do need to swap change the implentation class, then just change your code, it's not that hard. For 3) The dependency problem is made an a non issue because I programmed very clear application phases. Every service built into Stallion -- the database, the templating engine, JSON object mappers, etc. -- is either initialized in the boot phase or lazy-loaded. Thus it is always available. And it is always available when testing if you inherit the standard base class. You can always due TemplateRenderer.instance().render() and it will work.  4) YAGNI again.

Smart Caching
--------------------------------------------------------------------------------



Accessories Included
------------------------------------------------------------

*Helpers to get to and from JSON*




java -cp -agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:49788 -Dfile.encoding=Cp1252 -classpath "C:\Users\Saquil Mohamed\workspace\JMS\bin;C:\Users\Saquil Mohamed\Desktop\JMS\javax.jms-1.1.jar;C:\Users\Saquil Mohamed\Desktop\JMS\apache-activemq-5.2.0\activemq-all-5.2.0.jar" ch05.TBorrower TopicCF RateTopic 5.6
pause

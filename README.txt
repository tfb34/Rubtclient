Phase1 is complete. 

If you guys ran the program before, then you would have noticed that
the sound in the music video stopped playing around the 40sec mark. The problem was in the message.java
file. I kept trying to read 15350 bytes into a byte[] of length 16384. Anyway, you'll see
other changes. Manager is in charge of creating peers and launching threads for each one. Each peer has access to the
Manager's locked variables. They are called "locked" variables because in the case where we are downloading from multiple
peers we don't want them all to access these variables at the same time. My idea was that the manager will hold an array of piece indices or
integers. The peer threads are in charge of removing the top element and finding that piece. Before doing so, they first check
their bitfield to see if they have it. If they don't then they simply add the piece index/integer back to the queue, so that another 
peer can get a chance to find that piece. 

The program should work everytime without having to change the peers manually like I suggested last time. manager is in charge of
parsing the list of peers and only downloading from the one with the IP address "128.6.171.131". 

I also attached the output to this program. It's called booboo.mov. 


Compile:  javac RUBTClient.java

run: java RUBTClient Phase2.torrent musicVideo.mov

///////////////////////////new
Okay so I created a thread inside Manager to print the progress of our download. The download is also being timed 
inside the manager class.  The total download time is also printed to command prompt at the end of the download. 



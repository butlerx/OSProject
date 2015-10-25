# OSProject
Mp3 player for os project. All them buffers
# Learn to git
You should learn to git. Ask questions read docs


#Overview

In this assignment you will build on what you learned in labs and lectures to write a simple audio player applet in Java. Your solution must employ a producer-consumer design. One thread, the producer, will read chunks of audio data from a file and place them in a buffer. Another thread, the consumer, will read audio chunks from the buffer and write them to the audio device.
Solving your assignment will involve meeting the requirements listed below.
1. The Bounded Buffer Class

We have a buffer (i.e. an array) of a fixed size. The buffer is big enough to hold ten one-second chunks of audio data. A producer thread inserts chunks into the buffer, a consumer thread takes them out and writes them to the audio device. There are the usual constraints: the producer cannot insert into a full buffer, the consumer cannot take from an empty buffer, and updates to the buffer contents must be mutually exclusive. You must write the BoundedBuffer class. Suggested variables include:

    nextIn (integer)
    nextOut (integer)
    size (integer)
    occupied (integer)
    ins (integer)
    outs (integer)
    dataAvailable (boolean)
    roomAvailable (boolean)

When roomAvailable is false the buffer is full, when true there is at least one free slot available for the producer. When dataAvailable is false the buffer is empty, when true there is at least one chunk in the buffer for the consumer to remove. The buffer operates in a FIFO manner, i.e. the first chunk to go in is the first to come out.

Two pointers are associated with the buffer, nextIn and nextOut. nextIn points to a free slot in the buffer and indicates where the producer will insert the next audio chunk. nextOut points to the next audio chunk to be taken out of the buffer by the consumer (thereby producing a free slot). You need to be careful that nextIn and nextOut do not grow beyond the length of the buffer but wraparound instead. Modulo arithmetic will take care of that issue for you. A counter variable, occupied, keeps track of the number of audio chunks currently in the buffer.

The BoundedBuffer class should implement two methods, insertChunk and removeChunk which will be called by the producer and consumer threads respectively.
2. The Producer Class

Write the producer thread class. It should repeatedly read a chunk of audio data from the audio stream and write it to the bounded buffer.
3. The Consumer Class

Write the consumer thread class. It should repeatedly remove an audio chunk from the bounded buffer and write it to the audio device.
4. Thread Termination

Ensure that entering the 'x' command causes the program to exit. Before exiting each thread must be shut down cleanly, with the exiting thread printing a farewell message as it finishes.
5. Extra credit

In addition to the exit command, add support for the following:

    'q' raises the volume
    'a' lowers the volume
    'p' pauses playback
    'r' resumes playback
    'm' mutes audio output
    'u' unmutes audio output

#Resources

Playing audio with an applet
The Java Concurrency Tutorial
The Java Sound Tutorial
Deliverables

You are required to deliver the following:

    Your solution in StudentPlayerApplet.java
    design.txt
    problems.txt

Marks are awarded for solving the assignment, solving it efficiently and demonstrating your understanding of problems, issues and solutions through comments. Any shortcomings in your solution must be clearly identified. Marks will be deducted for:

    Overly-complicated solutions
    Failing to cleanly shutdown threads
    Excessive resource usage
    Busy-waiting
    Solutions that unnecessarily rely on calls to sleep to function correctly
    Commented out code
    Undocumented shortcomings
    Lines longer than 80 characters
    Unrequested additional "features"
    Failing to meet the requirements listed above

Provide a brief description of how your solution functions in design.txt. In problems.txt provide an explanation of any shortcomings or weaknesses in your solution. Failure to list any shortcomings indicates you are not aware of them and marks will be deducted accordingly.
What's it worth?

15% of your overall mark.
Do I have to work in a team?

Yes. Only submissions by teams with three or four members are acceptable. Submissions from teams with any other number of members will receive a mark of zero.
Plagiarism

See the School of Computing's Statement on Plagiarism. See also DCU's Academic Integrity and Plagiarism Policy. Submitting your assignment indicates that you have read and agree with the declaration in Appendix A of the plagiarism policy. You should note that the following are breaches of academic integrity that will be dealt with by the University's Disciplinary Committee:

    Sharing your work with anyone outside your group
    Posting your work on the Internet (either to request help or share with others)

#FAQ

Q. I am not in a project team. What should I do?
A. E-mail me as soon as possible and I'll try to put you in touch with other students in the same position. If you leave e-mailing me too late I will not accept your submission and you will receive a mark of zero.

Q. I was sick during the period we had to work on the assignment. Can I get an extension?
A. Yes, as long as your request for an extension is accompanied by a medical cert covering the period in question.

Q. I was sick during the period we had to work on the assignment but I did not visit a doctor so have no medical cert. Can I get an extension?
A. Any request for an extension not accompanied by a medical cert will be refused.

Q. What would a working solution look like?
A. It would look like this. (Note this video does not demonstrate all of the extra credit features.)

Q. Can you help us get started?
A. Yes. See the resources section.

Q. How will my solution be tested?
A. I will look at the code. I will also run it on a lab machine. You may develop your solution on your laptop but you should verify it functions correctly on a lab machine running Linux in L101 before submission.

Q. My solution is working on my laptop. However when I tested it ten minutes before the deadline on a machine in L101 it did not work properly. What can I do?
A. Nothing. You should have tested your code earlier.

Q. Can I use an ArrayBlockingQueue from the Java Collections Framework for my bounded buffer? Can I use an ArrayList?
A. No. You must implement your own bounded buffer using an array. That is why I listed some suggested variables.

Q. Should I use an applet-based approach?
A. You must use the applet-based approach. See the resources section. Using an applet will make it easier to handle user commands such as exit, pause, mute, etc.

Q. Can I use semaphores, locks, etc.?
A. No. You may only use synchronized, wait, notify and notifyAll.

Q. Can I use thread.stop to terminate my threads?
A. No. thread.stop is a deprecated method (i.e. Oracle does not recommend you use it because it does not work properly).

Q. I'm having trouble terminating my threads. What should I do?
A. Your priority should be, first off, to solve the bounded buffer problem and produce a working audio player applet. You will get marks for doing so. After that you can move on to supporting the exit command. After that you can move on to adding further commands for the extra credit.
I have a different question

E-mail me.
#How to submit

Create the following directory in one team member's Linux account:

    ~/ca321/assignment1-1516

Place all deliverables in this directory. Each file must contain the names and ID numbers of those students responsible for producing it. The contents of this directory will be automatically collected immediately the deadline expires.

You will lose write permission on the directory after collection. Also, a file named collected will appear in the directory and serves as verification that your assignment was successfully collected. If it does not appear, your assignment was not successfully submitted and you were unable to follow simple instructions.

It is your responsibility to ensure the directory exists only in one team member's account, is correctly named (directory and file names are case sensitive) and contains all assignment deliverables. To create the directory enter the following in a command shell:

    mkdir -p ~/ca321/assignment1-1516

#Late submissions

If you are unable to follow the assignment submission instructions and miss the deadline:

    E-mail all deliverables to me.
    Submissions received after 18:00 but before midnight on the due date are penalised 10%.
    An additional penalty of 10% per pay (or part of day) applies to submissions received after the due date.

#Deadline

Your solution will be collected from the above directory at 18:00 Tuesday 10 November 2015.


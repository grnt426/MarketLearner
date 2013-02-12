Market Learner
==============

The goal is to correlate movements in the Dow Jones Industrial Average with
movements on the NASDAQ Stock Market.  The general algorithm will be to use
AdaBoost with Decision Stumps. For now, to make sure all the various programs
can read/interpret data correctly, a really simple set of decision stumps is
generated to test program flow.

This project was completed for RIT's Artificial Intelligence class.

Defense of Options/Heuristics
-----------------------------

After testing several values for iterations, below ~47 and above ~48 the
accuracy on the training data would taper off.  In the extreme (around 200),
several weights would approach Infinity, suggesting that AdaBoost was starting
to over-fit some models.

The best performing MAX_STUMPS value (for choosing how many stumps to "vote"
when making a real prediction for a future day) was 5, with 10 a very close
second.  Five stumps was enough to model the original testing data with 78%
accuracy, ten stumps gave 77% accuracy, and 3 stumps performed with 74%
accuracy.

For predicting if the NASDAQ would go up or down, nothing fancy is done to the
total votes cast as satisfactory results were produced.

Decision Stumps
---------------
For now, decision stumps are really basic, and only contain a single kind.

* For an individual stock, the number of times the stock went up
(opening < closing)	and the NASDAQ went up. To normalize and form a weight,
divide by the number of instances counted.

Dependencies
------------
* Java 1.6.0_29 (tested on Windows 7 x64)

Source Control
--------------
https://github.com/grnt426/MarketLearner (Currently Private)
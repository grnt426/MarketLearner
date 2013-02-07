Market Learner
==============

The goal is to correlate movements in the Dow Jones Industrial Average with
movements on the NASDAQ Stock Market.  The general algorithm will be to use
AdaBoost with Decision Stumps.

Decision Stumps
---------------
For now, decision stumps are really basic, and the first round shall only
contain a single kind.

* For on individual stock, the number of times the stock went up
(opening < closing)	and the NASDAQ went up. To normalize and form a weight,
divide by the number of instances counted.
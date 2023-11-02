# AuctionSystem-AkkaActors

We mainly have 4 actors here which are AuctionSystem, SellerActor, EbayActor and
BankActor. Each actor is explained as under:

AuctionSystem:

This is where our program starts. We have created 3 sellers here and 10 products. We
afterwards created a list of objects from case class CreateAuction. On the CreateAuction case
class we put the name of seller and the product they want to put in auction. Then they are
sent to the seller actor. We put a time limit for the auctions to get create which is after
1000ms no more auctions will stay.

Again, we created a list of bidders and a list of objects which contains the bidders
name, the product they want to bid and their offer price. Then they are sent to the EbayActor
for bidding. Also, for bidders we put a time limit on which they must make the bid and pay or
else the bidding will not happen.

SellerActor:

SellActor receives messages from the AuctionActor. We have 2 cases here. One is
CreateAuction and another is DeleteAuction. Through the CreateAuction case a seller can
create auctions. If the seller decides to delete an auction, he can do it through the
deleteAuction case.

EbayActor:

Here it receives messages from bidder. Bidder can bid products on EbayActor. After
bidding the bid is updated on Ebay. EbayActor keep on giving messages on which is the highest
bid till now. When finally, the bidding is over Ebay sends the highest bidder’s name, bank
account number and product name to BankActor.

BankActor:

The BankActor has some list of registered bank accounts. It can check if the highest
bidder has a verified bank account. The bank also checks if for some if any bidding has only 1
or no bid. It notifies the seller about the product.

Description of the flow:

<img width="502" alt="Screenshot 2023-11-02 at 10 52 10 PM" src="https://github.com/Mishkat96/AuctionSystem-AkkaActors/assets/47037691/21aa28e9-6670-4ea8-bd72-e122794d8e83">

According to the diagram, first the auction starts from the AuctionSystem actor. From there,
the specified sellers go to the SellerActor and create their own Auctions. There is a time set
for each auction for its avaibility. The sellers send the product they want to sell, the initial
price and a list where all the bids will contain. Also, from the AuctionSystem a group of
speicifed bidders go to the EbayActor to bid for the products they want to buy. There is also
a time set for the bidders on which they must pay. The bidders send the name of the products
and the bidding price for a specific product. In the EbayActor they update the list given by the
seller about all the bidding prices. It also updates on Ebayy the highest bid of a specific
product. The EbayActor also keeps on notifying about the current highest bid made by a
bidder. The message then goes to the BankActor with the highest bidder’s name, bank
account number and for which product they made the bid. The bank checks the bidder’s bank
account number with the verified account number which it has. If the account number is
verified, then it sends a message to the bidder about his successful purchase. If the number
of bids is 1 or less, then the BankActor sends the product to the SellActor and the SellActor
deletes the product. The AuctionSystem also gets notified for products which were not paid
in time. The AuctionActor can make new auctions from those products again. Finally, from
the BankActor, it announces the winner of the bidder who made the highest bid and paid on
time.

Patters Used:

Mainly in this system Forward Flow pattern and Aggregator pattern has been used. The
AuctionSystem actor sending a message to EbayActor and it then forwarding the message to
BankActor is the example of Forward Flow in this architecture. Again, the BankActor sending
messages back to SellActor and AuctionSystem Actor is the example of Aggregator pattern in
this system.

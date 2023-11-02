package Assignment3

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DeathPactException, SupervisorStrategy}


trait AuctionSystem

case class Seller(name: String) extends AuctionSystem
case class Auction(item: String, price: Int, var AllBids: List[Int]) extends AuctionSystem
case class CreateAuction(seller: Seller, auction: Auction) extends AuctionSystem
case class DeleteAuction(auction: String) extends AuctionSystem
case class Bidder(name: String, bankAccountNum: Int) extends AuctionSystem
case class Ebay(auction: Auction,var highestBid: Int) extends AuctionSystem
case class RequestAuctions(ebay: Ebay, replyTo: ActorRef[AuctionSystem]) extends AuctionSystem
case class BidderNamePass(bidder: Bidder) extends AuctionSystem
case class BidderBid(bidder: Bidder, auction: Auction, priceOffer: Int) extends AuctionSystem
case class BankCheck(productName: String,price: Int, registeredName: String, registeredAccount: Int) extends AuctionSystem
case class Bank(registeredName: String, registeredAccount: Int) extends AuctionSystem


var Ebayy: List[Ebay] = List()
var product1 = Auction("Car", 1000, List())
var product2 = Auction("Fridge", 400, List())
var product3 = Auction("Oven", 200, List())
var product4 = Auction("SportsCar", 4000, List())
var product5 = Auction("Sofa", 500, List())
var product6 = Auction("Bed", 600, List())
var product7 = Auction("Lamp", 100, List())
var product8 = Auction("GucciShirt", 500, List())
var product9 = Auction("ComfySofa", 2500, List())
var product10 = Auction("AirConditioner", 1500, List())
var ListProds = List(product1,product2, product3, product4, product5, product6, product7, product8, product9, product10)

//actor for the auction system
object AuctionSystem:
  def apply(): Behavior[AuctionSystem] = Behaviors.setup { context =>

    context.log.info("Entering the system")

    val seller1 = Seller("Waseq")
    val seller2 = Seller("Ruslan")
    val seller3 = Seller("Pratik")

    val sellActor = context.spawnAnonymous(SellerActor())


    val createAuctionbySeller = List(
      CreateAuction(seller1, product1),
      CreateAuction(seller1, product2),
      CreateAuction(seller1, product3),
      CreateAuction(seller2, product4),
      CreateAuction(seller2, product5),
      CreateAuction(seller2, product6),
      CreateAuction(seller3, product7),
      CreateAuction(seller3, product8),
      CreateAuction(seller3, product9),
      CreateAuction(seller3, product10)
    )

    createAuctionbySeller.foreach(a => sellActor ! a)

    //After this time the auction will be closed and it cannot be bid anymore
    Thread.sleep(1000)


    val bidder1 = Bidder("John", 6574)
    val bidder2 = Bidder("Michael", 6372)
    val bidder3 = Bidder("Aaron", 7476)
    val bidder4 = Bidder("Jessica", 6273)
    val bidder5 = Bidder("Paul", 7637)



    val bidderBidding = List(
      BidderBid(bidder1, product1, 1100),
      BidderBid(bidder2, product1, 1200),
      BidderBid(bidder3, product1, 1600),
      BidderBid(bidder5, product4, 6000),
      BidderBid(bidder5, product3, 300),
      BidderBid(bidder2, product3, 400),
      BidderBid(bidder4, product2, 600),
      BidderBid(bidder4, product3, 700)
    )

    val bidActor = context.spawnAnonymous(EbayActor())
    bidderBidding.foreach(a => bidActor ! a)

    //Bidding has to be made in 1000ms or the bidding won't be accepted
    Thread.sleep(1000)



    Behaviors.same
  }


//Actor for Seller
object SellerActor:
  def apply(): Behavior[AuctionSystem] =
    Behaviors.setup { context =>
      context.log.info("Welcome to Sell Actor")

      Behaviors.receiveMessage { message =>
        message match
          case CreateAuction(sell, prod) =>
            context.log.info(s"${sell.name} has created a new auction for ${prod.item} with base price ${prod.price}")

            //Adding all auctions on Ebayy
            Ebayy = Ebayy :+ (Ebay(prod, prod.item.max))

            //Updating the products list with bids
            Behaviors.same

            //Delete auctions with less than 1 bidding
          case DeleteAuction(a) =>
            context.log.info(s"${a} will be no longer used for auction as it had only 1 bidding")
            Behaviors.same
      }
    }


//Actor for Ebay
object EbayActor:
  def apply(): Behavior[Any] =
    Behaviors.supervise {
      Behaviors.setup { context =>
        context.log.info("Welcome to Ebay Actor")
        val bankActor = context.spawnAnonymous(BankActor())

        Behaviors.receiveMessage { message =>
          message match
            case BidderBid(bidder, auction, priceOffer) =>

              val temp = Ebayy
              temp.foreach { temp =>
                if (auction.item == temp.auction.item) {
                  context.log.info(s"${bidder.name} has offered ${priceOffer} for the product ${auction.item}")

                  //Adding all prices on the list
                  val aucTemp = Ebayy.toList
                  aucTemp.foreach { temp =>
                    if (temp.auction.item == auction.item) {
                      temp.auction.AllBids = temp.auction.AllBids :+ priceOffer

                    }
                  }
                }
              }

              //Adding the highest bid
              val auctionsTemp = Ebayy.toList
              auctionsTemp.foreach { temp =>
                if (temp.auction.item == auction.item) {
                  if (temp.highestBid < priceOffer) {
                    temp.highestBid = priceOffer
                    val a = temp.highestBid
                    context.log.info(s"${a} is the highest bid for ${auction.item} till now")
                  }
                }
              }
              bankActor ! BankCheck(auction.item, priceOffer, bidder.name, bidder.bankAccountNum)
              Behaviors.same

            //incase Bidder fails to pay in time, the auction will be open again
            case BidderNamePass(bidder: Bidder) =>
              context.log.info(s"${bidder.name} has failed to pay in time. That's why the product is ready to be bid again")

              Behaviors.same
        }
      }
    }.onFailure[DeathPactException](SupervisorStrategy.restart)



object BankActor {
  def apply(): Behavior[AuctionSystem] =
    Behaviors.setup { (context) =>
      context.log.info("Entering the Bank actor")

      val bidLessThan1 = context.spawnAnonymous(SellerActor())
      //list of registered bank accounts
      val registeredBankAccounts = List(
        Bank("John", 6574),
        Bank("Aaron", 7476),
        Bank("Jessica", 6273)
      )

      Behaviors.receiveMessage { message =>
        message match
          case BankCheck(productName,price, registeredName, registeredAccountt) =>
            Ebayy.foreach(prod =>
            if(prod.auction.item == productName){
              if(prod.highestBid == price){
                registeredBankAccounts.foreach(p =>
                if(registeredAccountt == p.registeredAccount){
                  if(prod.auction.AllBids.length > 1) {
                    context.log.info(s"${registeredName} with ${registeredAccountt} bank account number has paid highest for ${productName} with price ${price}")
                    context.log.info(s"${registeredName} has successfully bought the product ${productName}")
                  }
                  else {
                    bidLessThan1 ! DeleteAuction(productName)
                  }
                })
              }
            }
            )

        Behaviors.same
      }
    }
}

object AuctionBiddingSystem extends App:
  val system: ActorSystem[AuctionSystem] = ActorSystem(AuctionSystem(), "AuctionSystem")

  system.terminate()
package Processes

import Startup.With
import Types.Allocations.Invoice

class Banker {
  val vault:Invoice = new Invoice(0,0,0)
  
  def tally() {
    vault.minerals  = With.game.self.minerals
    vault.gas       = With.game.self.gas
    vault.supply    = With.game.self.supplyTotal - With.game.self.supplyUsed
  }
  
  def reserve(invoice:Invoice) {
    vault.minerals -= invoice.minerals
    vault.gas -= invoice.gas
    vault.supply -= invoice.supply
  }
  
  def request(invoice:Invoice):Boolean = {
    vault.minerals  >= invoice.minerals &&
    vault.gas       >= invoice.gas &&
    vault.supply    >= invoice.supply
    
  }
  
  def tryToSpend(invoice:Invoice):Boolean = {
    val success = request(invoice)
    if (success) {
      reserve(invoice)
    }
    success
  }
}

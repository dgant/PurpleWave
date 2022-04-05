package ProxyBwapi

import bwapi.Race

trait Buildable {
  def race: Race
  def productionFrames(quantity: Int): Int
  def mineralCost(quantity: Int): Int
  def gasCost(quantity: Int): Int
  def supplyProvided: Int
  def supplyRequired: Int
}

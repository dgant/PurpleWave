package Strategery.Strategies.Terran

import Strategery.Strategies._

object TerranChoices {
  
  val tvr = Vector(
  )
  
  val tvtOpeners = Vector(
  )
  
  val tvpOpeners = Vector(
  )
  
  val tvzOpeners = Vector(
  )

  val all: Vector[Strategy] = (tvr ++ tvtOpeners ++ tvpOpeners ++ tvzOpeners).distinct
}
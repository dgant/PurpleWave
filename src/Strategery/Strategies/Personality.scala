package Strategery.Strategies

import Debugging.SimpleString

trait Personality extends SimpleString {
  def matches: Boolean
}
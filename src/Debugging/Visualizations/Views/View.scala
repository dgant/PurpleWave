package Debugging.Visualizations.Views

abstract class View {
  
  def unimplemented() {}
  
  def renderScreen(): Unit  = unimplemented
  def renderMap(): Unit     = unimplemented
  
  
  // Statefulness in a singleton! We avoid this because we don't want to maintain state across multiple games run consecutively.
  // But in this case it has nothing to do with the game and everything to do with testing across games.
  var enabled = false
}

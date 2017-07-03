package Debugging.Visualizations.Views

trait View {
  def unimplemented() {}
  
  def renderScreen(): Unit  = unimplemented
  def renderMap(): Unit     = unimplemented
  
  
}

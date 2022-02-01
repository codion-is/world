module is.codion.framework.demos.world.client {
  requires is.codion.swing.framework.ui;
  requires is.codion.plugin.jasperreports;
  requires com.formdev.flatlaf;
  requires org.jfree.jfreechart;
  requires org.json;

  requires is.codion.framework.demos.world.domain.api;

  //for loading reports from classpath
  opens is.codion.framework.demos.world.model;
}
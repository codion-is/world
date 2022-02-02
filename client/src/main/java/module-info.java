module is.codion.framework.demos.world.client {
  requires java.net.http;
  requires is.codion.swing.framework.ui;
  requires is.codion.plugin.jasperreports;
  requires is.codion.framework.demos.world.domain.api;
  requires com.formdev.flatlaf;
  requires com.formdev.flatlaf.intellijthemes;
  requires org.jfree.jfreechart;
  requires jasperreports;
  requires jxmapviewer2;
  requires org.json;

  exports is.codion.framework.demos.world.ui
          to is.codion.swing.framework.ui;

  //for loading reports from classpath
  opens is.codion.framework.demos.world.model;
}
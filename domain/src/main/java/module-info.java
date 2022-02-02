module is.codion.framework.demos.world.domain {
  requires transitive is.codion.framework.demos.world.domain.api;
  requires jxmapviewer2;

  exports is.codion.framework.demos.world.domain;

  provides is.codion.framework.domain.Domain
          with is.codion.framework.demos.world.domain.WorldImpl;
}
module is.codion.framework.demos.world.domain {
  requires java.desktop;
  requires transitive is.codion.framework.demos.world.domain.api;

  exports is.codion.framework.demos.world.domain;

  provides is.codion.framework.domain.Domain
          with is.codion.framework.demos.world.domain.WorldImpl;
}
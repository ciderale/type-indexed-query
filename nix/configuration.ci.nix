{pkgs, ...}: {
  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk25;
    gradle.enable = true;
    gradle.version = "9.6.1";
    gradle.hash = "sha256-nA9/ruswbLFOQnmj4ITKa1lolAiaBjjmigfJRaMsnhQ=";
  };

  treefmt.enable = true; # enable treefmt for formatting with multiple formatters
  treefmt.programs.alejandra.enable = true; #nix linter
  treefmt.programs.ktlint.enable = true;
  treefmt.pre-commit-hook = true;
}

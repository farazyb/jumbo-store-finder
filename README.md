# Jumbo Store Finder


## Time spent
- 1–2h understanding the requirements, comparing approaches, and
  writing CLAUDE.md before any code.



# Design Decisions
- In-memory, no database, because the dataset is static
- Haversine, hand-written, since curvature matters and it's the core logic
- Sort-and-take-5 rather than a spatial index, with a note on when I'd switch
- Framework-free domain as the layering rule
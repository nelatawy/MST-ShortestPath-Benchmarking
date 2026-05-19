# MST-ShortestPath Benchmarking

A benchmarked Java implementation of classic graph algorithms covering Minimum Spanning Trees, Single-Source Shortest Paths, and the supporting data structures on which these algorithms operate. The repository is organized into three modules: the core graph and algorithm logic, the input generation and benchmarking harness, and the JUnit 5 test suite.

---

## Project Structure

```
src/main/java/
├── DS/
│   ├── DisjointSet.java          # Disjoint-set interface
│   └── TreeDS.java               # Tree-based union–find implementation
├── graph/
│   ├── Edge.java                 # Weighted directed/undirected edge
│   └── Graph.java                # Generic graph + all algorithms
└── benchmarking/
    ├── Benchmarking.java         # Benchmark harness (warmup, measurement, CSV output)
    └── inputgeneration/
        ├── GraphGenerator.java   # Generator interface
        ├── GraphType.java        # Enum: SPARSE, DENSE, COMPLETE, DAG
        ├── DAGGenerator.java     # Directed acyclic graph generator
        ├── SparseGenerator.java  # Sparse undirected graph generator
        ├── DenseGenerator.java   # Dense undirected graph generator
        └── CompleteGenerator.java# Complete undirected graph generator

src/test/java/
├── DAGShortestPathTest.java
├── DijkstraShortestPathTest.java
├── MSTTest.java
├── GeneratorTest.java
└── TreeDSTest.java
```

---

## Graph Logic and Algorithms

### `Edge<E>`

A generic weighted edge implementing `Comparable<Edge<E>>` by weight. `equals` and `hashCode` are defined structurally on `(from, to)` only to make the `.contains()` work as intended — weight is intentionally excluded — so duplicate-edge detection in generators works correctly regardless of weight.

```java
public Edge(E from, E to, int weight)
```

### `DisjointSet<E>` and `TreeDS<E>`

`DisjointSet<E>` is an interface defining the standard union–find contract:

| Method | Description |
|---|---|
| `addSet(E rootEle)` | Creates a new singleton set |
| `getSet(E ele)` | Returns the representative of `ele`'s set |
| `unionSets(E first, E second)` | Merges the sets of `first` and `second` into `first`'s set |
| `getSetCount()` | Returns the current number of disjoint sets |

`TreeDS<E>` implements this with a parent-pointer forest backed by a `HashMap<E, TreeNode>`. Used by Kruskal's MST to maintain connected components.

> **Note:** `TreeDS` does not yet implement path compression . For very large graphs this gives O(n) worst-case `getSet` — a known improvement opportunity.

### `Graph<E>`

The central class. Maintains both an **adjacency list** (`Map<E, List<Edge<E>>> neighbors`) and a flat **edge list** (`List<Edge<E>> edges`) so it supports both node-centric and edge-centric algorithms efficiently.

#### Construction

```java
void addEdge(E from, E to, int weight)          // undirected: adds to both adjacency lists
void addDirectedEdge(E from, E to, int weight)  // directed: adds to `from` only
```

Both methods auto-register new nodes and increment `nodeCount`. An `edgesSorted` flag tracks whether the edge list is sorted by weight, allowing Kruskal to skip re-sorting on repeated calls.

#### Algorithms

**`primMST()`** — Prim's Algorithm, O((V + E) log V)

Grows the MST greedily from an arbitrary start node using a `PriorityQueue<EdgeRecord>`. Ignores stale PQ entries via a visited set, only pushes better candidates via a `minCost` map, Returns `null` for disconnected graphs.

**`kruskalMST()`** — Kruskal's Algorithm, O(E log E)

Sorts the edge list once (cached via `edgesSorted`) then uses `TreeDS` to greedily add edges that connect different components. Returns `null` for disconnected graphs. Outperforms Prim on sparse graphs; Prim wins on very dense ones (see benchmarks below).

**`dijkstra(E source)`** — Dijkstra's SSSP, O((V + E) log V)

Eager Dijkstra priority queue approach that only pushes entries if they provide an improved cost, Returns `null` immediately if any negative weight is detected. Unreachable nodes are absent from the returned map rather than set to infinity. Works on both directed and undirected graphs.

**`dagShortestPath(E source)`** — DAG Topological SSSP, O(V + E)

Runs Khan's algorithm to produce a topological ordering, then relaxes edges in that order. Because all predecessors of a node are processed before the node itself, each node's shortest path is finalized on first visit — no priority queue required. Supports negative edge weights (no negative cycles possible in a DAG). Unreachable nodes are absent from the returned map.

**`BFS(E source)`** — Breadth-First Search, O(V + E)

Returns a list of nodes in BFS order from `source`. Used internally by `GeneratorTest` to validate graph connectivity.

---

## Input Generation and Benchmarking

### Graph Generators

All generators implement the `GraphGenerator` interface:

```java
Graph<Integer> generate(int nodeCount);
```

Each seeds its `Random` with `42` for reproducibility. A spanning structure is always added first to guarantee connectivity before random edges are layered on top.

| Generator | Direction | Edge count formula | Notes |
|---|---|---|---|
| `SparseGenerator` | Undirected | E ≈ 5 × N | Cycle backbone + random edges |
| `DenseGenerator` | Undirected | E ≈ N(N−1)/8 | Quarter of a complete graph |
| `CompleteGenerator` | Undirected | E = N(N−1)/2 | All pairs |
| `DAGGenerator` | Directed | E ≈ 5 × N | Chain backbone; edges always go `from < to` to prevent cycles |

`DAGGenerator` enforces acyclicity by construction: for every random edge, `to` is chosen strictly greater than `from` so no back-edges are possible.

### Benchmarking Harness

`Benchmarking.java` provides three measurement methods and three population methods that drive them across a sweep of graph sizes.

**Warmup strategy:** 25 JIT warmup iterations run before any timing begins. This is important in Java because the JIT compiles hot methods after a threshold of invocations — without warmup, early measurements include interpretation overhead.

**Measurement:** Each algorithm is timed with `System.nanoTime()` and converted to milliseconds. Per-run times are collected into a list and summarized as mean, sample standard deviation, and median. Results are appended to CSV files.

| Method | Output file | Algorithms measured |
|---|---|---|
| `MSTConstructionBenchmark` | `mst-benchmarks.csv` | Prim, Kruskal |
| `generalShortestPathBenchmark` | `general-sssp-benchmarks.csv` | Dijkstra |
| `dagShortestPathBenchmarks` | `dag-sssp-benchmarks.csv` | DAG-Topo vs Dijkstra, with speedup ratio |

---

## JUnit Tests

All tests use JUnit 5 and are organized by algorithm. Each test class uses `@BeforeEach` to construct a fresh graph, isolating tests from each other.

### `MSTTest`

Covers both Prim and Kruskal across 7 test cases:

- Correct edge set and total weight on a known graph (expected weight: 7)
- `null` return on disconnected graphs
- Parallel edges — algorithms must pick the minimum-weight duplicate
- Negative weight edges (expected total: −3)
- Self-loop exclusion from the MST
- Empty graph and single-node graph edge cases

### `DijkstraShortestPathTest`

8 test cases including:

- Basic shortest paths with parallel edges
- Negative weight detection returning `null`
- Disconnected graph — reachable nodes present, unreachable absent
- Cycle handling without infinite loops
- Self-loops — distance to source stays 0
- Zero-weight edges
- Non-existent source node
- Single-node graph

### `DAGShortestPathTest`

5 test cases that specifically validate DAG-only behavior:

- Basic single-source shortest paths
- **Negative edge weights** — valid for DAGs, correctly handled (e.g. path cost of −1 through a −4 edge)
- Disconnected components — unreachable nodes return `null` in the map
- Multiple paths to the same destination — correct minimum selected
- Long linear chain — cumulative cost accumulates correctly

### `GeneratorTest`

Validates that all four generators produce connected graphs by running BFS from node `0` on a 100-node graph and asserting all 100 nodes are visited.

### `TreeDSTest`

3 tests covering `addSet`, `unionSets`, and `getSet` on `TreeDS<Integer>`.

---

## Benchmark Results

### DAG SSSP: DAG-Topological vs Dijkstra

Measured on DAG graphs with E ≈ 5 × N. Speedup = Dijkstra mean / DAG-Topo mean.

| Nodes | Edges | DAG-Topo mean (ms) | Dijkstra mean (ms) | Speedup |
|---|---|---|---|---|
| 1,000 | 5,000 | 0.27 | 0.49 | 1.81× |
| 2,500 | 12,500 | 0.64 | 0.91 | 1.43× |
| 5,000 | 25,000 | 1.18 | 3.41 | 2.89× |
| 7,500 | 37,500 | 1.67 | 11.49 | 6.87× |
| 10,000 | 50,000 | 2.31 | 7.38 | 3.19× |

The speedup advantage grows through 10,000 nodes as the O(V + E) vs O((V + E) log V) difference widens. The regression at 20,000 nodes reflects overhead in the topological sort phase (HashMap indirection and per-call `inDegrees` array allocation) becoming dominant at that scale — an active optimization target.

### Dijkstra SSSP: Performance by Graph Type

| Distribution | Nodes | Edges | Mean (ms) |
|---|---|---|---|
| DAG | 1,000 | 5,000 | 0.49 |
| DAG | 2,500 | 12,500 | 0.91 |
| DAG | 5,000 | 25,000 | 3.41 |
| DAG | 7,500 | 37,500 | 11.49 |
| DAG | 10,000 | 50,000 | 7.38 |
| Sparse | 1,000 | 5,000 | 0.87 |
| Sparse | 2,500 | 12,500 | 1.68 |
| Sparse | 5,000 | 25,000 | 3.87 |
| Sparse | 7,500 | 37,500 | 8.51 |
| Sparse | 10,000 | 50,000 | 15.80 |
| Dense | 1,000 | 124,875 | 15.10 |
| Dense | 2,500 | 780,937 | 120.99 |
| Dense | 5,000 | 3,124,375 | 361.99 |
| Dense | 7,500 | 7,030,312 | 722.70 |
| Complete | 1,000 | 499,500 | 60.23 |
| Complete | 2,500 | 3,123,750 | 201.82 |
| Complete | 5,000 | 12,497,500 | 857.08 |
| Complete | 7,500 | 28,121,250 | 2,081.72 |
| Complete | 10,000 | 49,995,000 | 3,878.03 |

Dijkstra scales cleanly on sparse graphs but degrades sharply on complete graphs due to priority queue pressure — O(E log V) with E = O(V²) gives O(V² log V) in the complete case.


### MST: Prim vs Kruskal (Native)

| Distribution | Nodes | Edges | Prim mean (ms) | Kruskal mean (ms) | Winner |
|---|---|---|---|---|---|
| Sparse | 500| 2500| 1.9156| 0.01939| Kruskal |
| Sparse | 1000| 5000| 4.141| 2.79| Kruskal |
| Dense | 500 | 31187 | 3.636 | 11.82 | Prim |
| Dense | 1000 | 124875 | 20.97537 | 49.664 | Prim |
| Complete | 500 | 124750 | 6.2404 | 39.0760632 | Prim |
| Complete | 1000 | 499500 | 35.750 | 221.9487 | Prim |

> **Note That** : The more dense the graph becomes the more Prim's should outperform Kruskal O((V+E)LgV) vs O(ELgE),
> However : if the benchmarks would run on the same edge list that was previously sorted so results were cached and therefore, the overhead of sorting was eliminated, kruskal might even be faster than prim.

package kube;

/**
 * Extremely high level algorithm for real time indexing and searching.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-06-2015
 */
public class Kube {

    // A) Index documents:
    // 1) Lock cluster
    // 2) Check table, last update timestamp
    // 3) Create document(Lucene) for changed/inserted/deleted row(s)
    // 4) Pop document(s) in the grid(batched, on topic) for changed and inserted, and the id for deleted rows
    // 5) Unlock grid
    // 6) For each grid listener write document to memory index, or delete using the id

    // B) Some time later, perhaps every minute or 10 000 memory documents or 1 gig:
    // 1) Start new index M'
    // 2) Write M to A
    // 3) Open searcher on M, M' & C
    // 4) Optimize index A
    // 5) Open searcher on M' & A
    // 6) Delete M
    // 7) Synchronize B with A
    // 8) Optimize B
    // 9) Synchronize C with B
    // 10 Optimize C

    // C) Continuously, every 5-60 seconds(for VERY near real time search):
    // 1) Open searcher on memory index(file system index already open)

    // D) In case of corrupt index:
    // 1) Open searcher on memory index and a not-corrupt index if open on corrupt index
    // 2) Delete the corrupt index, fast
    // 3) Copy one of the non corrupt indexes to the corrupt index position on the file system, slow

    // E) Jobs:
    // 1) Synchronize all servers indexes every 10 seconds, somehow...
    // 2) When synchronized, re-open on the synchronized index and set ready flag to true
    // 3) Check that index is open and not corrupt,
    //      if corrupt,
    //          set ready flag to false,
    //          delete index and wait for peer synchronize

    // F) On error for A:
    // A-1) Unlock grid
    // A-2) Unlock grid
    // A-3) Unlock grid
    // A-4) Unlock grid
    // A-5) Unlock grid
    // A-6) Will automatically synchronize in the synch job later

    // G) On error for B:
    // B-1) Retry...
    // B-2) If M & A are not corrupt, then retry,
    //      else drop M and wait for peer synchronize,
    //      set ready flag to false
    // B-3) If M, M' & C are not corrupt then retry,
    //      else delete corrupt index,
    //          open on non corrupt indexes,
    //          and wait for peer synchronize,
    //          set ready flag to false
    // B-4) Retry if index not corrupt,
    //      else wait for peer synchronize,
    //          set ready flag to false
    // B-5) Retry a few times,
    //      then wait for peer synchronize,
    //      set ready flag to false

    // TODO: Need to failover for all peers in the cluster...

}
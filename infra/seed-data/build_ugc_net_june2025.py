#!/usr/bin/env python3
"""
Builds infra/seed-data/ugc_net_june2025.json — UGC NET Computer Science, June 2025 (Paper 2).

Source: the paper + answer key the project owner supplied (95 of 100 questions). Explanations are written
for this platform, not copied from the coaching solutions. Each answer key was re-derived independently.

Left out on purpose (5): Q50 and Q75 need automata diagrams; Q95 needs matrix images whose options B and C
came out identical in the source; Q57 and Q92 were withdrawn by UGC and only exist here in a rewritten form.
"""
import json, os

TAG = "UGC NET June 2025"
DISK = "Consider the following disc Queue with requests for I/O to block on cylinders: 45, 81, 185, 33, 175, 99, 150, 77 (Consider that the head starts from location 58)\n"
TAC = ("100: t1=y+2\n101: initial = x/t1\n102: limit = 10\n103: if i > j goto 105\n104: goto 111\n"
       "105: if num > b goto 107\n106: goto 111\n107: num = limit-1\n108: b=y+2\n109: i = i - num\n"
       "110: goto 103\n111: j = i - num\n112: k=j+b")

TOPICS = [  # (key, topic name, description)
    ("OS", "Operating Systems", "Scheduling, memory, file systems, synchronisation, disk scheduling"),
    ("DSA", "Data Structures & Algorithms", "Trees, hashing, queues, recurrences, dynamic programming, greedy"),
    ("DBMS", "Database Management Systems", "Relational algebra, normal forms, keys, serializability"),
    ("CN", "Computer Networks", "Protocols, congestion, access methods, encoding"),
    ("TOC", "Theory of Computation", "Grammars and the Chomsky hierarchy"),
    ("DM", "Discrete Mathematics & Logic", "Counting, probability, propositional logic, groups"),
    ("SE", "Software Engineering", "Quality, coupling/cohesion, testing, COCOMO"),
    ("AI", "Artificial Intelligence & Soft Computing", "Agents, planning, genetic algorithms, fuzzy sets, neural networks"),
    ("C", "Programming in C", "Output prediction, pointers, operator precedence"),
    ("COA", "Computer Organization & Architecture", "Cache, I/O, pipelines, number systems, logic gates, RAID"),
    ("CD", "Compiler Design", "Parsers, three-address code, basic blocks"),
    ("CG", "Computer Graphics", "Clipping and projections"),
    ("SEC", "Information Security", "Cryptography basics"),
    ("MISC", "Data Mining, Web & Cloud", "Data mining, XML, virtualization"),
]

E, M, H = "EASY", "MEDIUM", "HARD"
QS = []  # (key, difficulty, text, options, answer letter, explanation, code)


def q(key, d, text, opts, ans, why, code=None):
    QS.append((key, d, text, opts, ans, why, code))


# ------------------------------------------------------------------ Operating Systems
q("OS", M, "Once the process is allocated CPU and executing which of the following events could not occur?\nA. The process could issue an I/O request and then be placed in the Ready Queue\nB. The process could create new subprocesses & wait for the termination of the subprocesses\nC. The time slice of the process expires and it may join the waiting queue\nD. The process is forcibly removed from the CPU and is put in the Waiting queue due to arrival of an interrupt\nChoose the correct answer from the options given below:",
  ["B Only", "D Only", "A and C Only", "A, C and D Only"], "D",
  "An I/O request sends a process to the waiting (blocked) queue, not the ready queue. An expired time slice or an interrupt-driven preemption puts it back on the ready queue, not the waiting queue. Only B can happen, so A, C and D are the events that cannot.")
q("OS", M, "What shall be the average waiting time per process if we know that 10 processes (on average) arrive every second and there are normally 20 processes in the queue?",
  ["03 seconds", "02 seconds", "18 seconds", "09 seconds"], "B",
  "Little's law: L = λ × W. With L = 20 and λ = 10 per second, W = 20 / 10 = 2 seconds.")
q("OS", E, "Which of the followings shows the correct hierarchy of a layered file system in an operating system\nA. Logical File System\nB. File Organization Module\nC. Basic File System\nD. I/O Control\nE. Application Programs\nChoose the correct answer from the options given below:",
  ["A → B → D → C → E", "D → E → C → A → B", "E → A → B → C → D", "E → C → B → A → D"], "C",
  "Application programs sit on top, followed by the logical file system, the file-organization module, the basic file system and finally I/O control (device drivers).")
q("OS", M, "Consider the following interrupt protection levels in Linux, and arrange them in the increasing order of their priorities.\nA. User-Mode Programs (Preemptible)\nB. Bottom Half Interrupt Handlers\nC. Top Half Interrupt Handlers\nD. Kernel System Service Routines (Preemptible)\nChoose the correct answer from the options given below:",
  ["B → A → C → D", "C → B → A → D", "C → A → B → D", "A → D → B → C"], "D",
  "Each level can be interrupted by the ones above it: user-mode programs < kernel service routines < bottom-half handlers < top-half handlers, which run immediately.")
q("OS", M, "Match List I with List II\nList I: A. Clustered Page Table; B. Hierarchical Page Table; C. Segmentation; D. Inverted Page Table\nList II: I. Generally considered inappropriate for 64 bit architectures. II. Has only entry for each real page (or frame) of memory. III. Useful for Sparse address spaces. IV. Supports a user view of the system.\nChoose the correct answer from the options given below:",
  ["A → III, B → IV, C → II, D → I", "A → III, B → I, C → IV, D → II", "A → II, B → III, C → I, D → IV", "A → IV, B → III, C → II, D → I"], "B",
  "Clustered page tables suit sparse address spaces; hierarchical tables become impractical for 64-bit spaces; segmentation matches the programmer's view of memory; an inverted table has one entry per physical frame.")
q("OS", M, "Identify the correct statement(s) from the followings with respect to Spinlock Semaphores\nA. The name refers to busy waiting semaphores.\nB. They are not useful when the locks are to be held for a short duration of time.\nC. It may require multiple context switches when a process waits on a lock.\nD. They are often employed on Uniprocessor systems.\nChoose the correct answer from the options given below:",
  ["A Only", "D Only", "B, D Only", "A, C and D Only"], "A",
  "A spinlock busy-waits, which avoids context switches and pays off for short holds on multiprocessors. B, C and D each claim the opposite, so only A is correct.")
q("OS", M, "What is the total swap time (Swap in Swap out) in a system for a 15 MB process with a transfer rate of 30 MBps. Given that there is an average latency of 12 ms, however no head seeks involved.",
  ["1.024 sec", "1.00 sec", "0.512 sec", "12 sec"], "A",
  "15 MB at 30 MB/s takes 0.5 s each way, so 1.0 s for swap-out plus swap-in. Average latency adds 2 × 12 ms = 0.024 s, giving 1.024 s.")
q("OS", H, "Consider the following table about processes, their burst time and arrival time\nProcess | Burst Time | Arrival Time\nP1 | 09 | 0\nP2 | 30 | 0\nP3 | 04 | 0\nP4 | 08 | 2\nP5 | 11 | 6\nNow which of the process shall finish second last as per the respective GANTT charts for the nonpreemptive SJF and Round Robin (time quantum =10 ) scheduling methods.",
  ["SJF: P4; RR: P4", "SJF: P5; RR: P4", "SJF: P4; RR: P3", "SJF: P5; RR: P5"], "D",
  "Non-preemptive SJF runs P3, P4, P1, P5, P2, so P5 finishes second last. Round robin (q = 10) finishes P1 (t=9), P3 (23), P4 (31), P5 (52), P2 (62), so P5 again.")
q("OS", M, "Which of the following statement is correct for Pthreads?",
  ["It refers to POSIX standard (IEEE 1002.1c)", "This standard defines API for thread creation only",
   "This is a specification only for thread behavior and not its implementation",
   "Operating systems like Solaris, Linux Mac OS X, except Tru64 UNIX, implement Pthreads"], "C",
  "Pthreads is a specification of thread behaviour, leaving the implementation to each OS. The standard is IEEE 1003.1c (not 1002.1c), it covers more than thread creation, and Tru64 UNIX also implements it.")
q("OS", M, DISK + "What shall be the 2nd request being processed by C-LOOK (Right) disc Scheduling technique for the already given request queue?",
  ["81", "45", "77", "33"], "A",
  "Moving right from 58, C-LOOK serves the pending requests above the head in order: 77 first, then 81.")
q("OS", H, DISK + "What shall be the total head movement of cylinders for the C-SCAN disc Scheduling technique if the head starts moving in the right direction from initial position ?",
  ["327", "307", "525", "385"], "D",
  "Assuming cylinders 0–199: 58 → 199 is 141, the return jump 199 → 0 is 199, then 0 → 45 is 45. Total 141 + 199 + 45 = 385.")
q("OS", M, DISK + "What shall be the 2nd request being processed by SCAN (LEFT) disc Scheduling technique for the already given request queue?",
  ["81", "15", "33", "175"], "C",
  "Moving left from 58, SCAN serves 45 first and 33 second.")
q("OS", H, DISK + "What will be the total head movement of cylinders for the SSTF disc Scheduling technique?",
  ["160", "124", "177", "240"], "C",
  "SSTF order is 45, 33, 77, 81, 99, 150, 175, 185: 13 + 12 + 44 + 4 + 18 + 51 + 25 + 10 = 177.")
q("OS", H, DISK + "What shall be the total head movement of cylinders if the FCFS disc scheduling method is used?",
  ["536", "594", "647", "700"], "C",
  "FCFS follows the arrival order: 13 + 36 + 104 + 152 + 142 + 76 + 51 + 73 = 647.")

# ------------------------------------------------------------------ Data Structures & Algorithms
q("DSA", E, "Consider the following infix expression Q: ((A + B) * D) ↑ (E − F). The equivalent postfix expression of Q is",
  ["AB+D*EF↑-", "AB+D*-↑EF", "AB+DE*F-↑", "AB+D*EF-↑"], "D",
  "(A + B) becomes AB+, multiplying by D gives AB+D*, (E − F) becomes EF−, and the final ↑ goes last: AB+D*EF−↑.")
q("DSA", M, "Which of the following trees are height balanced?\nA. Binary Search Tree\nB. AVL Tree\nC. Red-Black Tree\nD. B Tree\nChoose the correct answer from the options given below:",
  ["A and D Only", "A, B and D Only", "C and D Only", "B, C and D Only"], "D",
  "AVL trees, red-black trees and B-trees all guarantee logarithmic height. A plain binary search tree can degenerate into a chain.")
q("DSA", M, "Which of the following algorithms use Greedy strategy?\nA. Dijkstra's algorithm\nB. Kruskal's algorithm\nC. Huffman coding\nD. Bellman-Ford algorithm\nChoose the correct answer from the options given below:",
  ["A, B and D Only", "A, B and C Only", "C and D Only", "A and D Only"], "B",
  "Dijkstra, Kruskal and Huffman coding each repeatedly take the locally best choice. Bellman-Ford relaxes all edges repeatedly, which is a dynamic-programming approach.")
q("DSA", H, "The longest common subsequence of {1,2,3,2,4,1,2} and {2,4,3,1,2,1} is",
  ["2,1,2,3", "1,3,2,1", "2,3,2,1", "2,3,1,2,1"], "C",
  "2,3,2,1 appears in both sequences (positions 2, 3, 4, 6 of the first; 1, 3, 5, 6 of the second) and has length 4, the maximum. 2,3,1,2,1 isn't a subsequence of the first, and 1,3,2,1 and 2,1,2,3 aren't subsequences of the second/first respectively.")
q("DSA", M, "Match List I with List II\nList I (Hashing Collision Handling Method): A. Chaining; B. Linear Probing; C. Quadratic Probing; D. Double Hashing\nList II (Strategy): I. Check next slot; II. Use the second hash function; III. Linked list at index; IV. Skip slots using quadratic step\nChoose the correct answer from the options given below:",
  ["A-II, B-III, C-I, D-IV", "A-II, B-III, C-IV, D-I", "A-III, B-I, C-IV, D-II", "A-IV, B-II, C-I, D-III"], "C",
  "Chaining keeps a linked list per slot, linear probing tries the next slot, quadratic probing steps by squares, and double hashing uses a second hash function to get the step size.")
q("DSA", E, "Maintaining a graph in memory by means of its adjacency matrix is known as",
  ["Complete Representation", "Linked Representation", "Circular Representation", "Sequential Representation"], "D",
  "An adjacency matrix is an array, so the graph is stored sequentially. Adjacency lists are the linked representation.")
q("DSA", M, "Match List I with List II\nList I: A. Circular Queue; B. Priority Queue; C. Double Ended Queue; D. Simple Queue\nList II: I. Print Queue; II. CPU Scheduling; III. Dijkstra algorithm; IV. Palindrome checking\nChoose the correct answer from the options given below:",
  ["A-II, B-III, C-I, D-IV", "A-II, B-III, C-IV, D-I", "A-III, B-II, C-I, D-IV", "A-IV, B-II, C-I, D-III"], "B",
  "Circular queues underlie round-robin CPU scheduling, priority queues drive Dijkstra's algorithm, deques suit palindrome checking, and a simple queue models a print queue.")
q("DSA", H, "The tight asymptotic bound for the recurrence T(n) = 2T(n/4) + √n is",
  ["Θ(√n)", "Θ(n log n)", "Θ(√n log n)", "Θ(n log √n)"], "C",
  "Master theorem with a = 2, b = 4: n^(log₄ 2) = √n, which matches f(n) = √n exactly. That is the balanced case, so T(n) = Θ(√n log n).")
q("DSA", M, "When developing a dynamic programming algorithm, the sequence of steps followed is:\nA. Construct an optimal solution from computed information.\nB. Recursively define the value of an optimal solution.\nC. Characterize the structure of an optimal solution.\nD. Compute the value of an optimal solution, typically in a bottom-up fashion.\nChoose the correct answer from the options given below:",
  ["B, C, A, D", "B, A, C, D", "C, B, A, D", "C, B, D, A"], "D",
  "Characterize the structure, define the value recursively, compute it bottom-up, then construct the solution.")
q("DSA", M, "The correct sequence of constructing Huffman tree is\nA. Repeat until root formed\nB. Create leaf nodes\nC. Build priority queue\nD. Combine lowest frequency nodes\nChoose the correct answer from the options given below:",
  ["B, C, A, D", "D, B, A, C", "B, C, D, A", "C, A, B, D"], "C",
  "Create a leaf per symbol, load them into a priority queue, repeatedly merge the two lowest-frequency nodes, and stop when a single root remains.")

# ------------------------------------------------------------------ DBMS
q("DBMS", M, "Consider the following three relations Employee (eid, eName), Comp(cid, cName), Own(eid, cid). Which of the following relational algebra expression return the set of eids who own all brands:",
  ["πeid(πeid,cid(Own) / πcid(Comp))", "πeid(πeid(Own) × πcid(Comp))", "πeid(πeid,cid(Own) × πcid(Comp))", "πeid(πeid(Own) × (πcid,Name(Own) / πcid(Comp)))"], "A",
  "\"Owns all\" is the classic use of relational division: the (eid, cid) pairs divided by the set of all cids leave exactly the eids that appear with every cid.")
q("DBMS", H, "Consider R(A, B, C, D, E) be relation with following dependencies: C->F, E->A, EC->D, A->B. Which of the following is a key for R ?",
  ["CD", "EC", "AE", "AC"], "B",
  "Closure of EC: E→A, A→B, EC→D and C→F give A, B, C, D, E (and F), so EC determines everything. CD, AE and AC each miss an attribute.")
q("DBMS", H, "Consider the relation T1(A, B, C, D, E) with the dependencies {EB → C, D → E, EA → B} and T2(A, B, C, D) with the dependencies {C → A, A → B, A → D}. Which of the following is TRUE?",
  ["T1 is in 3NF", "T2 is in 3NF", "T 1 is not in 3NF", "T1 is in 2NF"], "C",
  "The only key of T1 is DA. EB → C has a non-superkey left side and a non-prime right side, so T1 violates 3NF (D → E is also a partial dependency, so it isn't in 2NF either). T2's key is C, and A → B violates 3NF there as well.")
q("DBMS", M, "In a relational database, which one of the following is CORRECT:",
  ["A relation with only two attributes is always in BCNF.", "If all attributes of a relation are prime attributes then the relation in BCNF",
   "Every relation has at least one non-prime attribute.", "BCNF decomposition preserves functional dependencies."], "A",
  "With two attributes, any non-trivial dependency has a key on its left, so BCNF always holds. All-prime attributes only guarantees 3NF, a relation can consist entirely of prime attributes, and BCNF decomposition may lose dependencies.")
q("DBMS", M, "Considering the following statements:\nA. A non-serial schedule is said to be conflict serializable, if it is conflict-equivalent to some serial schedule.\nB. A non-serial schedule is said to be view serializable if it is view-equivalent to some serial schedule.\nC. A schedule is said to be serial, if instructions of participating transactions are chronologically interleaved with each other.\nD. A conflict-serializable schedule will be view-serializable also but vice-versa may not be true.\nChoose the correct answer from the options given below:",
  ["A, B, C, D", "A, B, C Only", "A, B, D Only", "B, C, D Only"], "C",
  "A, B and D are correct. C is wrong: a serial schedule runs each transaction completely before the next, with no interleaving.")

# ------------------------------------------------------------------ Computer Networks
q("CN", M, "Match List I with List II\nList I: A. Count to infinity problem; B. IEEE 802.16; C. IEEE 802.15; D. AODV protocol\nList II: I. Mobile Adhoc Networks; II. Distance vector routing; III. WiMAX; IV. Bluetooth\nChoose the correct answer from the options given below:",
  ["A-II, B-III, C-I, D-IV", "A-II, B-III, C-IV, D-I", "A-II, B-IV, C-III, D-I", "A-IV, B-III, C-I, D-II"], "B",
  "Count-to-infinity affects distance-vector routing; IEEE 802.16 is WiMAX; IEEE 802.15 covers Bluetooth-style personal area networks; AODV is a routing protocol for mobile ad hoc networks.")
q("CN", E, "CB84000D001C001C is the content of a UDP header in hexadecimal format. The source port number is",
  ["52100", "13", "28", "52000"], "A",
  "The first two bytes of a UDP header are the source port: 0xCB84 = 12×4096 + 11×256 + 8×16 + 4 = 52100.")
q("CN", E, "In a Stop-and-wait system, the bandwidth of the line is 1 Mbps , and 1 bit takes 30 milliseconds to make a round trip. The bandwidth-delay product is",
  ["15,000 bits", "20,000 bits", "30,000 bits", "60,000 bits"], "C",
  "Bandwidth × round-trip delay = 1,000,000 bits/s × 0.030 s = 30,000 bits.")
q("CN", E, "The packet sent by a node to the source to inform it of congestion is called",
  ["Explicit", "Backpressure", "Choke", "Retransmission"], "C",
  "A choke packet is the message a congested node sends back to the traffic source asking it to slow down.")
q("CN", M, "Which of the following are controlled-access protocols\nA. Reservation\nB. Polling\nC. TDMA\nD. Token Passing\nE. CSMA/CA\nChoose the correct answer from the options given below:",
  ["A and D Only", "A, B and D Only", "C, D and E Only", "A, D and E Only"], "B",
  "Reservation, polling and token passing are controlled-access methods. TDMA is channelization and CSMA/CA is random access.")
q("CN", E, "Arrange the steps involved when a user enters a URL in a browser and accesses a website.\nA. DNS resolution\nB. HTTP request sent\nC. URL entered in the browser\nD. IP address obtained\nE. Webpage displayed\nChoose the correct answer from the options given below:",
  ["B, A, C, E, D", "C, B, A, E, D", "B, C, D, A, E", "C, A, D, B, E"], "D",
  "The URL is entered, DNS resolves the name, the IP address is obtained, the HTTP request is sent, and the page is displayed.")
q("CN", E, "Which of the following is not the characteristics of packet switching?",
  ["Data is broken into packets", "Each packet may take a different route", "Requires a dedicated path", "Supports dynamic routing"], "C",
  "A dedicated end-to-end path is the hallmark of circuit switching. Packet switching routes packets independently.")
q("CN", M, "Which of the following are examples of data encoding schemes?\nA. Non-Return to Zero (NRZ)\nB. Manchester Encoding\nC. Amplitude Modulation\nD. Hamming code\nE. Bipolar AMI\nChoose the correct answer from the options given below:",
  ["A, C and D Only", "A, B and D Only", "C, D and E Only", "A, B and E Only"], "D",
  "NRZ, Manchester and bipolar AMI are line-coding schemes. Amplitude modulation is analog modulation and the Hamming code is an error-correcting code.")

# ------------------------------------------------------------------ Theory of Computation
q("TOC", H, "Which of the following Grammars is/are only Context Free?\nA: S → Ab, aS → aA, A → a\nB: S → Ab, A → Sa, A → a\nC: S → AS, S → aA, A → a\nD: S → Ab, S → aA, A → a\nE: S → Sb, A → Aa, A → ε\nChoose the correct answer from the options given below:",
  ["A, B Only", "C, D Only", "C, E Only", "A, B, D, E Only"], "B",
  "A has aS → aA, which isn't a context-free rule. B and E use only left-linear rules, so they are regular. C (S → AS) and D (a mix of left- and right-linear rules) are context-free but not regular.")
q("TOC", M, "Match List I with List II\nList I: A. Type 3 Grammar; B. Type 2 Grammar; C. Type 1 Grammar; D. Type 0 Grammar\nList II: I. V → (V ∪ Σ)*; II. λVβ → λ(V ∪ Σ)*β, where (λ, β) ∈ (V ∪ Σ)*; III. (V ∪ Σ)* → (V ∪ Σ)*; IV. V → VΣ | Σ\nChoose the correct answer from the options given below:",
  ["A → II, B → III, C → IV, D → I", "A → III, B → I, C → IV, D → II", "A → IV, B → I, C → II, D → III", "A → III, B → II, C → I, D → IV"], "C",
  "Type 3 (regular) rules look like V → VΣ | Σ; type 2 (context-free) has a single variable on the left; type 1 (context-sensitive) rewrites a variable in context λ…β; type 0 (unrestricted) allows any string on either side.")

# ------------------------------------------------------------------ Discrete Mathematics & Logic
q("DM", E, "In a pack of 42 cards, 3 cards are chosen one after the other. Find the number of ways this can be done without replacement:",
  ["1722", "1752", "68880", "6880"], "C",
  "The order matters and there is no replacement, so the count is P(42, 3) = 42 × 41 × 40 = 68,880.")
q("DM", E, "A positive integer selected at random from the set of positive integers not exceeding 200. Which of the followings is the probability that the selected number is divisible by either 2 or 5 ?",
  ["2 / 5", "3 / 5", "4 / 5", "1 / 5"], "B",
  "Divisible by 2: 100; by 5: 40; by both (10): 20. So 100 + 40 − 20 = 120 numbers out of 200, a probability of 3/5.")
q("DM", M, "Match List I with List II\nList I: A. If the Indian team wins, then it is raining; B. If the Indian team does not win, then it is not raining; C. If it is raining, then the Indian team wins; D. If it is not raining, then the Indian team does not win\nList II: I. Inverse; II. Converse; III. Contrapositive; IV. Conditional\nChoose the correct answer from the options given below:",
  ["A-II, B-I, C-IV, D-III", "A-III, B-I, C-II, D-IV", "A-IV, B-I, C-II, D-III", "A-III, B-II, C-IV, D-I"], "C",
  "Take P = the Indian team wins, Q = it is raining. A is P → Q (conditional), B is ¬P → ¬Q (inverse), C is Q → P (converse), D is ¬Q → ¬P (contrapositive).")
q("DM", M, "Let P denote \"She is intelligent\" and let Q denote \"She is happy.\"\nGiven below are the statements:\n(a) If She is intelligent, then She is unhappy.\n(b) She is neither intelligent nor happy.\n(c) It is necessary to be not intelligent in order to be happy.\n(d) To be not intelligent is to be unhappy.\nWhich of the following is correct Propositional expression for the above statements:",
  ["(a) P → ¬Q; (b) P ∧ ¬Q; (c) Q → ¬P; (d) P → ¬Q", "(a) P → Q; (b) ¬P ∧ ¬Q; (c) ¬Q → P; (d) ¬P → ¬Q",
   "(a) P → ¬Q; (b) ¬P ∧ ¬Q; (c) Q → ¬P; (d) ¬P → ¬Q", "(a) P → ¬Q; (b) P ∧ ¬Q; (c) Q → P; (d) ¬P → ¬Q"], "C",
  "(a) P → ¬Q. (b) ¬P ∧ ¬Q. (c) Being happy requires not being intelligent: Q → ¬P. (d) ¬P → ¬Q.")
q("DM", E, "In a group of 120 people: 65 eat Rice, 45 eat bread, 42 eat curd, 20 eat both Rice and bread, 25 eat both Rice and curd, 15 eat both bread and curd, and 8 eat all three items. Which of the following is the number of people who eat at least one of the three items:",
  ["56", "100", "92", "65"], "B",
  "Inclusion–exclusion: 65 + 45 + 42 − 20 − 25 − 15 + 8 = 100.")
q("DM", H, "Choose the correct statement for a group G :",
  ["If for all x, y ∈ G, (xy)² = x²y² then G is Commutative.", "If for all x ∈ G, x³ = 1, then G is Commutative. 1 is the identity element of G.",
   "If for all x ∈ G, x⁵ = 1, then G is Commutative. 1 is the identity element of G.", "If G is Commutative, the sub-group of G need not be Commutative."], "A",
  "(xy)² = x²y² means xyxy = xxyy; cancelling x on the left and y on the right gives yx = xy, so G is abelian. Exponent 3 or 5 does not force commutativity, and every subgroup of an abelian group is abelian.")
q("DM", M, "Let m and n are positive integers. Then\n(A) If n ≠ 1, then m < mn.\n(B) If k is composite, then k = mn where 1 < m, n > k.\n(C) If mn = 1, then m = 1 and n = 1.\n(D) If k is composite, then k = mn where 1 < m, n < k.\nWhich of the following is correct:",
  ["(A), (C), (D)", "(B), (C), (D)", "(A), (B)", "(A), (B), (C)"], "A",
  "(A) holds because n ≥ 2 gives mn ≥ 2m > m. (C) holds since positive integers with product 1 must both be 1. (D) is the definition of a composite number. (B) is impossible because m and n cannot both exceed k.")

# ------------------------------------------------------------------ Software Engineering
q("SE", M, "Match List I with List II\nList I (Software Quality Characteristic): A. Reliability; B. Efficiency; C. Maintainability; D. Portability\nList II (Description): I. Ability to transfer the software from one organization or hardware / software environment to another. II. Capability of software to maintain its level of performance under stated conditions for a stated period of time. III. Relationship between the level of performance of the software and the amount of resources used, under stated conditions. IV. Effort needed to make modifications, including corrections, improvements or adaptation of software to changes in environment, requirements and functional specifications.\nChoose the correct answer from the options given below:",
  ["A-II, B-III, C-I, D-IV", "A-II, B-III, C-IV, D-I", "A-III, B-II, C-I, D-IV", "A-IV, B-II, C-I, D-III"], "B",
  "Reliability is sustained performance over time; efficiency is performance relative to resources used; maintainability is the effort to modify; portability is ease of moving to another environment.")
q("SE", E, "The process followed in order to find difficult, unknown and hidden information about a software system is called",
  ["Software Engineering", "Software Re-Engineering", "Reverse Engineering", "Inverse Engineering"], "C",
  "Reverse engineering analyses an existing system to recover its design and hidden details.")
q("SE", E, "Which of the followings is NOT a software characteristic?",
  ["Software does not wear out", "Software is flexible", "Software is not manufactured", "Software is always correct"], "D",
  "Software doesn't wear out, is easily changed, and is developed rather than manufactured. It certainly is not always correct.")
q("SE", M, "Which of the following cohesions are better than the Procedural Cohesion\nA. Functional Cohesion\nB. Sequential Cohesion\nC. Temporal Cohesion\nD. Communicational Cohesion\nE. Logical Cohesion\nChoose the correct answer from the options given below:",
  ["A and D Only", "A, B and D Only", "C, D and E Only", "A, D and E Only"], "B",
  "From best to worst: functional, sequential, communicational, procedural, temporal, logical, coincidental. So functional, sequential and communicational beat procedural cohesion.")
q("SE", M, "Which of the followings belongs to McCall's quality factors?\nA. Maintainability\nB. Usability\nC. Integrity\nD. Functionality\nChoose the correct answer from the options given below:",
  ["A and D Only", "A, B and D Only", "C and D Only", "A, B and C Only"], "D",
  "McCall's factors include maintainability, usability and integrity. Functionality is not one of them.")
q("SE", E, "Estimation of software development effort for organic software in basic COCOMO is",
  ["E = 2.4(KLOC)^1.05 PM", "E = 3.4(KLOC)^1.06 PM", "E = 2.0(KLOC)^1.05 PM", "E = 2.4(KLOC)^1.07 PM"], "A",
  "Basic COCOMO organic mode: E = 2.4 × (KLOC)^1.05 person-months.")
q("SE", M, "Match List I with List II\nList I: A. Equivalence Partitioning; B. Boundary Value Analysis; C. Cyclomatic Complexity; D. Decision Table Testing\nList II: I. Measures independent paths in code; II. Divides input into valid/invalid sets; III. Focuses on limits of input ranges; IV. In which a number of combinations of actions are tested under varying sets of conditions.\nChoose the correct answer from the options given below:",
  ["A-II, B-III, C-I, D-IV", "A-II, B-III, C-IV, D-I", "A-III, B-II, C-I, D-IV", "A-IV, B-II, C-I, D-III"], "A",
  "Equivalence partitioning splits inputs into valid/invalid classes, boundary value analysis targets the edges of ranges, cyclomatic complexity counts independent paths, and decision tables cover condition–action combinations.")
q("SE", E, "Which of the following is not a requirement elicitation Technique",
  ["Interviews", "The use case approach", "Facilitated Application Specification Technique (FAST)", "Data Flow diagram"], "D",
  "Interviews, use cases and FAST all gather requirements from stakeholders. Data-flow diagrams model a system once requirements are known.")
q("SE", M, "Arrange the following in the increasing order of coupling from lowest coupling to highest coupling.\nA. Common Coupling\nB. Stamp Coupling\nC. Control Coupling\nD. External Coupling\nE. Content Coupling\nChoose the correct answer from the options given below:",
  ["E, A, C, B, D", "D, B, A, E, C", "B, C, D, A, E", "C, A, B, D, E"], "C",
  "Coupling grows from stamp to control to external to common, and content coupling is the tightest.")
q("SE", E, "Arrange the following types of testing in the order they are usually performed in the software Development life cycle.\nA. Integration testing\nB. Unit testing\nC. System Testing\nD. Acceptance Testing\nChoose the correct answer from the options given below:",
  ["B, C, A, D", "B, A, C, D", "C, B, A, D", "C, B, D, A"], "B",
  "Testing grows outward: unit, then integration, then system, then acceptance.")

# ------------------------------------------------------------------ AI & Soft Computing
q("AI", M, "Consider the following statements regarding Agent systems\nA. Agent system comprises of an agent and an environment on which it acts\nB. The controller part of an agent receives percepts from its body and sends commands to the environment\nC. Agents act in the world through actuators which are non-noisy and always reliable.\nD. The actuators of an agent convert stimuli into percepts\nChoose the correct answer from the options given below:",
  ["A, B Only", "B, D Only", "C, D Only", "B, C, D Only"], "A",
  "C is false because actuators can be noisy and unreliable; D is false because sensors, not actuators, turn stimuli into percepts. That leaves A and B (per the official key).")
q("AI", E, "Consider the following steps used by a knowledge base designer to represent a world\nA. Selects atoms to represent propositions\nB. Ask questions about intended interpretation\nC. Choose a task domain\nD. Axiomatizing the domain\nChoose the correct answer from the options given below:",
  ["C → A → D → B", "C → A → B → D", "B → C → A → D", "A → C → B → D"], "A",
  "Choose the domain, pick atoms for propositions, axiomatise the domain, then ask questions about the intended interpretation.")
q("AI", M, "Match List I with List II\nList I (GA termination criteria): A. Worst Individual; B. Best Individual; C. Sum of Fitness; D. Median Fitness\nList II (description): I. At least half of the individual will be better than or equal to convergence value; II. Guarantees that virtually all individuals in the population will be within a particular fitness range; III. Guarantees that the entire population to be of a minimum standard; IV. Faster search Convergence, guaranteeing at least one best solution\nChoose the correct answer from the options given below:",
  ["A → III, B → IV, C → II, D → I", "A → IV, B → III, C → I, D → II", "A → II, B → I, C → III, D → IV", "A → II, B → III, C → IV, D → I"], "A",
  "Stopping on the worst individual guarantees a minimum standard for everyone; on the best individual gives fast convergence with at least one best solution; on the sum of fitness keeps nearly all individuals within a fitness range; on the median means at least half are at or above the convergence value.")
q("AI", H, "Consider the following table defining the sample inputs and corresponding target values for a perceptron model.\nSample | x1 | x2 | target\nS1 | 0 | 0 | 0\nS2 | 0 | 1 | 1\nS3 | 1 | 0 | 1\nS4 | 1 | 1 | 1\nWhat shall be the value of updated weights after applying all the samples S1 to S4 (in the order S1, S2, S3, S4) to this model. Given that the initial weights w1=0, w2=0, learning rate =0.1 and no bias is involved in the perceptron. The activation function for this perceptron is: y_observed = 1 if y_in > 0; 0 if y_in < 0",
  ["w1=0.1, w2=0.1", "w1=0.0, w2=0.2", "w1=0.0, w2=0.1", "w1=0.2, w2=0.2"], "A",
  "Taking y_in = 0 as output 0: S1 needs no change; S2 gives y = 0 vs target 1, so w2 becomes 0.1; S3 similarly makes w1 = 0.1; S4 gives y_in = 0.2, y = 1, no change. Final w1 = w2 = 0.1.")
q("AI", M, "Consider the following steps involved in the application of Genetic algorithm for a problem\nA. Select a pair of parents from the population\nB. Apply mutation at each locus with probability pm\nC. Calculate fitness of each member of the population\nD. Apply crossover with probability pc to form offsprings\nChoose the correct answer from the options given below describing the correct order of the above steps:",
  ["A → C → B → D", "C → A → D → B", "C → A → B → D", "A → D → B → C"], "B",
  "Evaluate fitness, select parents, apply crossover, then apply mutation.")
q("AI", E, "Which of the followings is Not a parent selection technique used in genetic algorithm implementations",
  ["Radial", "Tournament", "Boltzmann", "Rank"], "A",
  "Tournament, rank and Boltzmann selection are standard parent-selection methods; \"radial\" selection is not.")
q("AI", M, "Match List I with List II\nList I (Operations on Fuzzy Sets): A. Intersection; B. Bounded Sum; C. Bounded Difference; D. Algebraic Sum\nList II (Description): I. μA(x) + μB(x) − μA(x)·μB(x); II. max(0, μA(x) − μB(x)); III. min(1, μA(x) + μB(x)); IV. min(μA(x), μB(x))\nChoose the correct answer from the options given below:",
  ["A → III, B → I, C → II, D → IV", "A → III, B → II, C → IV, D → I", "A → IV, B → III, C → II, D → I", "A → II, B → I, C → III, D → IV"], "C",
  "Intersection is the minimum, bounded sum is min(1, a + b), bounded difference is max(0, a − b), and algebraic sum is a + b − ab.")
q("AI", H, "Which of the followings is not a valid property over two fuzzy relations R and S which must be obeyed for performing λ-cut defuzzifications?",
  ["(R ∪ S)λ = Rλ ∪ Sλ", "(R ∩ S)λ = Rλ ∩ Sλ", "(R̄)λ ≠ R̄λ, except when λ = 1", "For any λ ≤ β, where 0 ≤ β ≤ 1, it is true that Rβ ⊆ Rλ"], "C",
  "λ-cuts commute with union and intersection, and a higher threshold gives a smaller cut (option D). For complements the λ-cut generally differs from the complement of the cut, and the usual exception is λ = 0.5, not λ = 1, so option C is not a valid property.")
q("AI", M, "Given that η refers to learning rate and xi refers to the i-th input to the neuron, Which of the followings most suitably describes the weight updation rule of a Kohonen SOM? (where 'j' refers to the j-th neuron in the lattice)",
  ["wij(new) = (1 − η)·wij(old) + η·xi", "wii(new) = η·wii(old) + (1 − η)·xi", "wij(new) = (1 − η)·wij(old) + xi", "wij(new) = wij(old) + η·xi"], "A",
  "The Kohonen rule moves the weight toward the input: w_new = w_old + η(x − w_old) = (1 − η)·w_old + η·x.")
q("AI", M, "Consider the following statements regarding STRIPS representation of a Planning problem\nA. STRIPS is a Feature-centric representation\nB. The features describing state of the world are divided into Primitive and Derived\nC. The STRIPS representation of the action comprises of Precondition and Effect\nD. STRIPS can directly define conditional effects.\nChoose the correct answer from the options given below:",
  ["A, C Only", "B, C Only", "A, D Only", "B, C, D Only"], "B",
  "STRIPS is action-centric (not feature-centric), splits features into primitive and derived, defines each action by a precondition and an effect, and cannot express conditional effects directly.")

# ------------------------------------------------------------------ Programming in C
q("C", E, "What will be the output of the following C programming code:", ["1", "1 2", "1 3", "1 1 3"], "A",
  "The outer loop runs for i = 1 and i = 3. For i = 1 the inner loop doesn't execute; for i = 3 it prints j = 1 (j = 3 fails j < 3). The output is a single 1.",
  code="int i, j;\nfor(i=1; i<5; i+=2)\n  for(j=1; j<i; j+=2)\n    printf(\"%d\", j);")
q("C", E, "What would be the equivalent pointer expression for referring the array element ar[ m ][ n ][0]",
  ["*(*(*(ar)+m+n)+0)", "(*(*(*ar+m)+n)+0)", "(*(*(ar+m)+n)+0)", "*(*(*(ar+m)+n)+0)"], "D",
  "Each subscript becomes one dereference: ar[m] is *(ar+m), ar[m][n] is *(*(ar+m)+n), and ar[m][n][0] is *(*(*(ar+m)+n)+0).")
q("C", M, "Consider the following piece of C programming code. Which one will be the output?", ["18", "2", "92", "74"], "B",
  "The loop is the subtraction form of Euclid's algorithm and stops when x = y = gcd(128, 110) = 2.",
  code="int x=128, y=110;\ndo\n{\n  if(x>y)\n    x=x-y;\n  else\n    y=y-x;\n} while(x!= y);\nprintf(\"%d\",x);")
q("C", M, "What will be the output of the following C Programming code :", ["91", "90", "98", "92"], "B",
  "The comma expression evaluates left to right: a = 14, b = 7, a = 98, b = 91, so c = 91. Then --(*i) decrements c to 90.",
  code="void main ()\n{\n  int *i,a=12,b=2,c;\n  c=(a=a+b,b=a/b,a=a*b,b=a-b);\n  i=&c;\n  printf(\"%d\", --(*i));\n}")
q("C", M, "Only legal pointer operations:\nA. pointer + number → pointer\nB. pointer - number → number\nC. pointer + pointer → pointer\nD. pointer - pointer → pointer\nE. pointer - pointer → number\nChoose the most appropriate answer from the options given below:",
  ["A, B, C Only", "A, B, D Only", "A, B Only", "A, E Only"], "D",
  "pointer + number gives a pointer and pointer − pointer gives an integer count. pointer − number gives a pointer (not a number), and adding two pointers is illegal.")
q("C", M, "Consider the operators used in C Programming given below:\nA. &&\nB. +=\nC. >>\nD. >=\nE. ?:\nChoose among the following the correct order of precedence of the operators given above (higher to lower):",
  ["C, D, E, B, A", "C, A, D, E, B", "C, D, A, E, B", "D, C, A, B, E"], "C",
  "From higher to lower precedence: shift (>>), relational (>=), logical AND (&&), conditional (?:), compound assignment (+=).")

# ------------------------------------------------------------------ Computer Organization & Architecture
q("COA", E, "The transformation of data from main memory to cache memory is referred as:", ["Data exchange", "Data transformation", "Mapping", "Matching"], "C",
  "Cache mapping (direct, associative or set-associative) decides where a main-memory block is placed in the cache.")
q("COA", M, "Consider the Boolean expression A(x, y, z) = x(y'z)'. Which of the following is complete sum-of-products form of the given Boolean expression.",
  ["xyz + x'yz' + x'y'z'", "xyz' + x'yz' + x'y'z'", "xyz' + xy'z + x'y'z'", "xyz + xyz' + xy'z'"], "D",
  "x(y'z)' = x(y + z') = xy + xz'. The expression is 1 for xyz, xyz' and xy'z' (x = 1 with y = 1 or z = 0).")
q("COA", M, "Match List I with List II\nList I: A. Address Space; B. Memory Space; C. Cache Memory; D. Segmented Program\nList II: I. Associative Mapping; II. Logical address; III. physical address; IV. Virtual address\nChoose the correct answer from the options given below:",
  ["A-IV, B-III, C-I, D-II", "A-I, B-II, C-IV, D-III", "A-III, B-IV, C-I, D-II", "A-II, B-IV, C-I, D-III"], "A",
  "Address space means virtual addresses, memory space means physical addresses, associative mapping is a cache technique, and a segmented program uses logical addresses.")
q("COA", E, "Binary equivalent to (A0F)16 is :", ["111000111", "101001111", "101000001111", "111100001010"], "C",
  "Convert each hex digit to four bits: A = 1010, 0 = 0000, F = 1111, giving 101000001111.")
q("COA", E, "Gray code equivalent to decimal number 8 is:", ["1000", "1100", "1010", "1110"], "B",
  "8 = 1000 in binary. The Gray code keeps the leading bit and XORs each bit with the one before it: 1, 1⊕0 = 1, 0⊕0 = 0, 0⊕0 = 0, i.e. 1100.")
q("COA", M, "The write operation in I/O operation does the following-",
  ["Transfer data from I/O device to memory", "Transfer data from memory to I/O device", "Transfer data from CPU register to memory", "Transfer data from CPU register to I/O device"], "B",
  "A write (output) operation sends data outward to the device. This follows the paper's key; some texts phrase it as CPU register to device, so wording matters here.")
q("COA", E, "Which of the following gates do not give output 1 when both the inputs are 0 -\nA. NAND gate\nB. NOR gate\nC. X-OR gate\nD. X-NOR gate\nChoose the correct answer from the options given below:",
  ["A, B Only", "B, C Only", "C, D Only", "C Only"], "D",
  "With inputs 0, 0: NAND = 1, NOR = 1, XOR = 0, XNOR = 1. Only XOR fails to give 1.")
q("COA", M, "Match List I with List II\nList I: A. RAID Level 2; B. RAID Level 3; C. RAID Level 5; D. RAID Level 6\nList II: I. Block interleaved distribution parity; II. Also known as P+Q redundancy Scheme; III. Bit interleaved parity; IV. Also known as Memory style error correcting code organization\nChoose the correct answer from the options given below:",
  ["A → IV, B → III, C → I, D → II", "A → III, B → IV, C → I, D → II", "A → I, B → III, C → IV, D → II", "A → II, B → IV, C → I, D → III"], "A",
  "RAID 2 uses Hamming-style ECC, RAID 3 has bit-interleaved parity, RAID 5 has block-interleaved distributed parity, and RAID 6 uses P + Q redundancy.")
q("COA", E, "The computer needs to process each instruction with the following sequence of steps:\nA. Calculate the effective address\nB. Fetch the Instruction from memory\nC. Decode the instruction\nD. Fetch the operands from the memory\nE. Execute the Instruction\nChoose the correct answer from the options given below:",
  ["A, B, C, D, E", "B, A, D, A, E", "B, C, A, D, E", "C, B, A, D, E"], "C",
  "The instruction cycle is fetch, decode, compute the effective address, fetch the operands, execute.")
q("COA", M, "Which of the following statements are true ?\nA. In the write-through policy, only the cache is updated\nB. In the write-back policy, both cache and main memory are updated\nC. Cache coherence problems exist in multiprocessors with private caches because of the need to share writable data\nD. Cache coherence problem can be solved by means of hardware-only scheme\nChoose the correct answer from the options given below:",
  ["A, B Only", "B, C Only", "C, D Only", "A, B, C Only"], "C",
  "Write-through updates both cache and memory, and write-back updates memory later, so A and B are false. Shared writable data in private caches creates coherence problems, and hardware schemes such as snooping and directories can solve them.")
q("COA", M, "The right sequence of CPU program to input data will be :\nA. Read status register\nB. Check flag bit\nC. Read Data Register\nD. Transfer data to memory\nChoose the correct answer from the options given below:",
  ["A, B, C, D", "B, A, C, D", "C, B, A, D", "A, C, B, D"], "A",
  "Programmed input: read the status register, check the flag, read the data register, then store the data in memory. (One published solution notes the official key differed from this order.)")
q("COA", E, "The right sequence of suboperations that are performed in arithmetic pipeline is-\nA. Align the mantissas\nB. Add or subtract the mantissas\nC. Normalize the result\nD. Compare the exponents\nChoose the correct answer from the options given below:",
  ["D, A, B, C", "D, B, A, C", "B, C, A, D", "B, C, D, A"], "A",
  "Floating-point addition pipeline: compare exponents, align mantissas, add or subtract, then normalize.")

# ------------------------------------------------------------------ Compiler Design
q("CD", M, "Arrange the following parsers in increasing order of their power of handling grammars i.e. from the least powerful parser to the most powerful parser.\nA. LR (0)\nB. LR (1)\nC. LALR(1)\nD. LL (0)\nE. SLR\nChoose the correct answer from the options given below:",
  ["LL(0) → LR(0) → SLR → LR(1) → LALR(1)", "SLR → LR(0) → LL(0) → LR(1) → LALR(1)", "LL(0) → LR(0) → SLR → LALR(1) → LR(1)", "LR(0) → LL(0) → SLR → LR(1) → LALR(1)"], "C",
  "Power increases LL(0) < LR(0) < SLR < LALR(1) < LR(1).")
q("CD", M, "Directions (91-95): Read the passage and answer the given questions. Consider the Three Address code shown below.\nWhat are the various LEADERS (leading statements) for the TAC given in the passage?",
  ["100,102,103,104,105,107,110", "100,103,105,107,111,112", "100,103,105,106,107,111", "100,103,104,105,106,107,111"], "D",
  "Leaders are the first statement (100), every jump target (103, 105, 107, 111) and every statement right after a jump (104, 105, 106, 107, 111): 100, 103, 104, 105, 106, 107, 111.",
  code=TAC)
q("CD", H, "Directions (91-95): Read the passage and answer the given questions. Consider the Three Address code shown below.\nWhich of the following is the correct High level code for the given TAC in the passage",
  ["t1 = y+2; init = x/t1; limit=10; if(i>j || num>b) { num = limit-1; b = y + 2; i=i-num; } j=i - num; k = k+b;",
   "init = x/(y+2); limit=10; while(i>j || num>b) { num = limit-1; b = y + 2; i=i -num; } j = i - num; k = k+b;",
   "init = x/(y+2); limit=10; if(i>j) { While( num>b) num = limit-1; } else { b = y + 2; i=i-num; } j= i - num; k= k+b:",
   "init = x/(y+2); limit=10; while(i>j && num>b) { num = limit-1; b=y+2; i=i-num; } j = i - num; k = k+b;"], "D",
  "The body (107–109) runs only if both i > j (line 103) and num > b (line 105) hold, and then jumps back to 103, which is a while loop with &&. (All four options print the last line as k = k+b, which looks like a typo for k = j+b in the TAC.)",
  code=TAC)
q("CD", H, "Directions (91-95): Read the passage and answer the given questions. Consider the Three Address code shown below.\nWhat all blocks does Block 3 and Block 5 dominate in the Flow graph for the TAC given in the passage?",
  ["Block 3: 3; Block 5: 5", "Block 3: 3,4,5,6; Block 5: 5,6", "Block 3: 3,7; Block 5: 5,7", "Block 3: 3,4,7; Block 5: 5,6,7"], "A",
  "Blocks in order: B1 = 100–102, B2 = 103, B3 = 104, B4 = 105, B5 = 106, B6 = 107–110, B7 = 111–112. B7 can be reached through either B3 or B5, so neither dominates it, and each dominates only itself.",
  code=TAC)

# ------------------------------------------------------------------ Computer Graphics
q("CG", E, "Which of the following technique is used for Clipping?", ["Stack based Seed", "Scan Line Seed", "Sutherland-Cohen", "Inverse Scaling"], "C",
  "Sutherland–Cohen (Cohen–Sutherland) is a line-clipping algorithm that uses region outcodes. The seed methods are fill algorithms.")
q("CG", M, "Match List I with List II\nList I: A. Oblique Projection; B. Cavalier Projection; C. Cabinet Projection; D. Orthographic Projection\nList II: I. When the direction of projection is so chosen that the lines perpendicular to the plane of projection are foreshortened. II. When the direction of projection is so chosen that there is no foreshortening of lines perpendicular to the plane of projection. III. When the direction of projection is perpendicular to the plane of projection. IV. When the angle between the projectors and the plane of projection is not equal to 90°.\nChoose the correct answer from the options given below:",
  ["A-III, B-II, C-IV, D-I", "A-IV, B-II, C-I, D-III", "A-III, B-I, C-IV, D-II", "A-IV, B-I, C-III, D-II"], "B",
  "Oblique projectors are not perpendicular to the plane (angle ≠ 90°); cavalier keeps true length of perpendicular lines; cabinet foreshortens them; orthographic projectors are perpendicular.")

# ------------------------------------------------------------------ Information Security
q("SEC", E, "Match List I with List II\nList I: A. Digital Signature; B. Hash Function; C. AES; D. RSA\nList II: I. Asymmetric encryption algorithm. II. Confirms authenticity and integrity. III. Produces a fixed-size digest. IV. Symmetric encryption algorithm.\nChoose the correct answer from the options given below:",
  ["A-II, B-III, C-I, D-IV", "A-II, B-III, C-IV, D-I", "A-III, B-II, C-I, D-IV", "A-IV, B-II, C-I, D-III"], "B",
  "Digital signatures give authenticity and integrity, hash functions produce a fixed-size digest, AES is symmetric, and RSA is asymmetric.")
q("SEC", M, "Which of the following statements is/are TRUE about encryptions techniques?\nA. In symmetric key cryptography, the secrecy/protection of the key generating function must be of higher level than the decrypting function.\nB. The DES technique works by taking 64 bit chunk value and 56 bit key over a series of transformations.\nC. The Twofish symmetric block algorithm works on variable key length of upto 196 bits over a block of 128 bits.\nD. RC4 is the most commonly used stream cipher that encrypts & decrypts a stream of bits/bytes instead of blocks.\nChoose the correct answer from the options given below:",
  ["B, C Only", "A, D Only", "A, C Only", "B, D Only"], "D",
  "DES uses 64-bit blocks with a 56-bit effective key (B) and RC4 is a widely used stream cipher (D). It is the key that must stay secret, not the key-generation function (A false), and Twofish keys go up to 256 bits, not 196 (C false).")

# ------------------------------------------------------------------ Data Mining, Web & Cloud
q("MISC", M, "Arrange the process of virtualization in cloud environments.\nA. Hypervisor installed on physical server\nB. Virtual machines created\nC. Resources allocated to Virtual machines\nD. Virtual machines run isolated workloads\nChoose the correct answer from the options given below:",
  ["B, C, A, D", "A, B, C, D", "A, C, B, D", "B, C, D, A"], "B",
  "Install the hypervisor, create the virtual machines, allocate resources to them, then run isolated workloads.")
q("MISC", M, "Considering the following statements:\nA. Data transformation is involved in Datamining process.\nB. Online database is used in Data warehouse.\nC. Classification is a measure of accuracy.\nD. K-means clustering algorithm is based on the concept of minimizing the within-cluster variance.\nE. Pattern evaluation is a process to identify knowledge based on interestingness measure.\nChoose the correct answer from the options given below:",
  ["B, C, E Only", "A, D, E Only", "A, C, E Only", "A, C, D Only"], "B",
  "A, D and E are true. A data warehouse is an analytical (OLAP) store rather than an \"online database\", and classification is a technique, not a measure of accuracy.")
q("MISC", M, "Consider the following statements for XML (Extensible Markup Language) :\nA. The number of tags decreases and users can define their own tags.\nB. Its used for Presentation.\nC. It is case sensitive.\nD. It is dynamic.\nWhich of the above statements are true ?",
  ["B, C, D Only", "A, B, D Only", "A, C, D Only", "B, D Only"], "C",
  "Per the official key: XML lets users define their own tags, is case-sensitive and flexible/dynamic, and is not meant for presentation (that is HTML/CSS's job).")

# ================================================================== build
assert len(QS) == 95, len(QS)
topics_out = []
for key, name, desc in TOPICS:
    qs = []
    for k, d, text, opts, ans, why, code in QS:
        if k != key: continue
        item = {"d": d, "q": text, "options": opts, "answer": "ABCD".index(ans), "why": why, "tags": [TAG]}
        if code: item["code"] = code
        qs.append(item)
    topics_out.append({"name": f"June 2025 — {name}", "description": desc, "questions": qs})

data = {
    "category": {"code": "UGC_NET", "name": "UGC NET"},
    "subjects": [{
        "name": "Paper 2 — Previous Year: June 2025",
        "description": "UGC NET Computer Science, June 2025 — 95 of the 100 questions (diagram-based and withdrawn questions left out)",
        "topics": topics_out,
    }],
    "exams": [{
        "title": "UGC NET Computer Science — June 2025 (95 of 100 questions)",
        "duration_minutes": 120, "navigation_mode": "FREE", "negative_marking": 0,
        "sections": [{"name": t["name"].replace("June 2025 — ", ""), "topics": [t["name"]], "count": len(t["questions"])} for t in topics_out],
    }],
}
out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "ugc_net_june2025.json")
json.dump(data, open(out, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print("wrote", out, "-", sum(len(t["questions"]) for t in topics_out), "questions,", len(topics_out), "topics")

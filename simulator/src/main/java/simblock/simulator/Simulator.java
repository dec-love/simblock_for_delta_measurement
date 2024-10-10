/*
 * Copyright 2019 Distributed Systems Group
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simblock.simulator;

import static simblock.settings.SimulationConfiguration.NUM_OF_NODES;
import static simblock.simulator.Main.INPUT_FILE;
import static simblock.simulator.Timer.getCurrentTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import simblock.block.Block;
import simblock.node.Node;

/**
 * The type Simulator is tasked with maintaining the list of simulated nodes and
 * managing the block
 * interval. It observes and manages the arrival of new blocks at the simulation
 * level.
 */
public class Simulator {

  /** A list of nodes that will be used in a simulation. */
  private static final ArrayList<Node> simulatedNodes = new ArrayList<>();

  /** The target block interval in milliseconds. */
  private static long targetInterval;

  /**
   * Get simulated nodes list.
   *
   * @return the array list
   */
  public static ArrayList<Node> getSimulatedNodes() {
    return simulatedNodes;
  }

  /**
   * Get target block interval.
   *
   * @return the target block interval in milliseconds
   */
  public static long getTargetInterval() {
    return targetInterval;
  }

  /**
   * Sets the target block interval.
   *
   * @param interval - block interval in milliseconds
   */
  public static void setTargetInterval(long interval) {
    targetInterval = interval;
  }

  /**
   * Add node to the list of simulated nodes.
   *
   * @param node the node
   */
  public static void addNode(Node node) {
    simulatedNodes.add(node);
  }

  /**
   * Remove node from the list of simulated nodes.
   *
   * @param node the node
   */
  @SuppressWarnings("unused")
  public static void removeNode(Node node) {
    simulatedNodes.remove(node);
  }

  /**
   * Add node to the list of simulated nodes and immediately try to add the new
   * node as a neighbor
   * to all simulated nodes.
   *
   * @param node the node
   */
  @SuppressWarnings("unused")
  public static void addNodeWithConnection(Node node) {
    node.joinNetwork();
    addNode(node);
    for (Node existingNode : simulatedNodes) {
      existingNode.addNeighbor(node);
    }
  }

  /** A list of observed {@link Block} instances. */
  private static final ArrayList<Block> observedBlocks = new ArrayList<>();

  /**
   * A list of observed block propagation times. The map key represents the id of
   * the node that has
   * seen the block, the value represents the difference between the current time
   * and the block
   * minting time, effectively recording the absolute time it took for a node to
   * witness the block.
   */
  private static final ArrayList<LinkedHashMap<Integer, Long>> observedPropagations = new ArrayList<>();

  /**
   * Handle the arrival of a new block. For every observed block, propagation
   * information is
   * updated, and for a new block propagation information is created.
   *
   * @param block the block
   * @param node  the node
   */
  public static void arriveBlock(Block block, Node node) {
    // ブロックの高さが0のときはスルー（ジェネシスブロックなので．ジェネシスブロックの高さは0）
    if (block.getHeight() == 0) {
      // System.out.println("block.getHeight() == 0");
      return;
    }
    // If block is already seen by any node
    if (observedBlocks.contains(block)) {
      // Get the propagation information for the current block
      LinkedHashMap<Integer, Long> propagation = observedPropagations.get(observedBlocks.indexOf(block));
      // Update information for the new block
      propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
    } else {
      // If the block has not been seen by any node and there is no memory allocated
      // TODO move magic number to constant
      if (observedBlocks.size() > 10) {
        // After the observed blocks limit is reached, log and remove old blocks by FIFO
        // principle
        printPropagation(observedBlocks.get(0), observedPropagations.get(0));
        observedBlocks.remove(0);
        observedPropagations.remove(0);
      }
      // If the block has not been seen by any node and there is additional memory
      LinkedHashMap<Integer, Long> propagation = new LinkedHashMap<>();
      propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
      // Record the block as seen
      observedBlocks.add(block);
      // Record the propagation time
      observedPropagations.add(propagation);
    }
  }

  /**
   * Print propagation information about the propagation of the provided block in
   * the format:
   *
   * <p>
   * <em>node_ID, propagation_time</em>
   *
   * <p>
   * <em>propagation_time</em>: The time from when the block of the block ID is
   * generated to when
   * the node of the <em>node_ID</em> is reached.
   *
   * @param block       the block
   * @param propagation the propagation of the provided block as a list of
   *                    {@link Node} IDs and
   *                    propagation times
   */
  public static void printPropagation(Block block, LinkedHashMap<Integer, Long> propagation) {
    // Print block and its height
    // TODO block does not have a toString method, what is printed here
    // 全ノードにpropagationされてなかったら終了
    if (propagation.size() < NUM_OF_NODES) {
      System.out.println("akira");
      return;
    }
    // arrivedBlockですでにやっている
    // if (block.getHeight() == 0) {
    // System.out.println("block.getHeight() == 0");
    // return;
    // }

    // 100ブロックごとに高さを出力
    if (block.getHeight() % 100 == 0)
      System.out.println(block + ":" + block.getHeight());
    // 今着目しているpropagationで取り扱われているブロックのマイナーを取得し，そいつブロック生成回数を更新する
    Node from = block.getMinter();
    from.propCntUpdate();
    for (Map.Entry<Integer, Long> timeEntry : propagation.entrySet()) {
      // System.out.println(timeEntry.getKey() + "," + timeEntry.getValue());
      // ブロックを生成したマイナーから各ノードへのpropagation時間を更新する．(あとでpropした回数，つまり，ブロックを生成した回数で割る必要あり)
      Integer toNodeId = timeEntry.getKey();
      long propTime = timeEntry.getValue();
      from.propUpdate(toNodeId, propTime);
    }
    // System.out.println();
  }

  /**
   * Print propagation information about all blocks, internally relying on {@link
   * Simulator#printPropagation(Block, LinkedHashMap)}.
   */
  public static void printAllPropagation() {
    // observedBlocks.size()の数だけしか作業しないのでココはコメントアウト．printPropagation()はarriveBlocks()で呼ばれるので，そこでpropとpropCntを更新する．と思ったが，observedBlocks.size()>10となっており，最後の10ブロックはpropとpropCnt処理されていないのでここで，それら10個を処理する必要がある．つまり，コメントアウトしない．
    for (int i = 0; i < observedBlocks.size(); i++) {
      printPropagation(observedBlocks.get(i), observedPropagations.get(i));
    }

    // inputファイルに出力していく
    // まずノード数を出力
    // INPUT_FILE.println(NUM_OF_NODES);

    // 次に各ノードのハッシュレートとリージョンを出力
    // for (Node node : getSimulatedNodes()) {
    // INPUT_FILE.print(node.getMiningPower());
    // INPUT_FILE.print(" ");
    // INPUT_FILE.print(node.getRegion());
    // INPUT_FILE.println();
    // }

    // 次に各ノードのpropagation時間の行列を出力
    for (Node from : getSimulatedNodes()) {
      for (Node to : getSimulatedNodes()) {
        if (from.getPropCnt() != 0) {
          // propに格納している値は，propagation時間の合計なので，propagation時間の平均を出力するためにpropagation回数で割る
          // ノードidは考えなくてよい．0から順番に左から出力していくだけ
          INPUT_FILE.print(from.getPropTime(to.getNodeID()) / from.getPropCnt());
          INPUT_FILE.print(" ");
        }
      }
      INPUT_FILE.println();
    }

    // 出力ストリームは効率的に記入するため，バッファにたくわえておき定期的にファイルに書き込む．強制的にバッファにあるものを書き込むため，flush()を呼び出す．
    INPUT_FILE.flush();

  }
}

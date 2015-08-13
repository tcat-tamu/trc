package edu.tamu.tcat.trc.entries.types.bio.test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 *  Draw random samples from source data in which the source data observations are associated
 *  with some weight.
 *
 */
public class WeightedObservationHeapSampler<T>
{
   private static final String ERR_MESSAGE_SAMPLE_SIZE_LESS_THAN_ZERO = "Cannot select {0} elements. Sample size must be greater than zero.";
   private static final String ERR_MESSAGE_SAMPLE_SIZE_TO_BIG = "Cannot select {0} elements. Sample size must be smaller than the number of source elements [{1}].";
   // See http://stackoverflow.com/questions/2140787/select-k-random-elements-from-a-list-whose-elements-have-weights

   private RandomDataGenerator random = new RandomDataGenerator();
   private WeightedItem<T>[] nodes;

   /**
    *
    * @param items The observed items to be used to base selections on.
    * @param adapter A function that generates weights for the supplied items.
    */
   @SuppressWarnings("unchecked")
   public WeightedObservationHeapSampler(Collection<T> items, ToDoubleFunction<T> adapter)
   {
      List<WeightedItem<T>> temp = new ArrayList<>();
      temp.add(null);                                    // leave first element empty
      temp.addAll(items.parallelStream()
                   .map(item -> new WeightedItem<T>(item, adapter))
                   .collect(Collectors.toList()));
      nodes = new WeightedItem[temp.size()];
      nodes = temp.toArray(nodes);

      for (int ix = nodes.length - 1; ix > 1; ix--)
      {
         nodes[ix >> 1].totalWeight += nodes[ix].totalWeight;
      }
   }

   /**
    * Draws {@code k} observations from the underlying data observations without replacement.
    * Consequently, individual instances from the source data may appear multiple times in the
    * resulting list.
    *
    * @param k The number of items to draw.
    * @return {@code k} items randomly sampled from the source data.
    */
   public List<T> sampleWithReplacement(int k)
   {
      checkSampleSize(k, true);
      return IntStream.range(0, k)
            .parallel()
            .mapToObj((i) -> {
               double weight = random.nextUniform(0, nodes[1].totalWeight);
               int ix = select(nodes, weight);
               return nodes[ix].item;
            })
            .collect(Collectors.toList());
   }

   /**
    * Draws {@code k} observations from the underlying data observations without replacement.
    * Consequently, individual instances from the source data will not appear more than once
    * in the resulting list.
    *
    * @param k The number of items to draw.
    * @param withReplacement Indicates whether values should be replaced in the source data
    *       once they have been sampled. If {@code false}, individual instances from the source
    *       data will not appear more than once in the resulting list.
    * @return {@code k} items randomly sampled from the source data.
    */
   public List<T> sampleWithoutReplacement(int k)
   {
      checkSampleSize(k, false);

      // clone the data so that multiple calls do not modify the source data
      @SuppressWarnings("unchecked")
      WeightedItem<T>[] data = new WeightedItem[nodes.length];
      System.arraycopy(nodes, 0, data, 0, nodes.length);

      return IntStream.range(0, k)
            .mapToObj((i) -> {
               double weight = random.nextUniform(0, data[1].totalWeight);
               int ix = select(data, weight);
               removeNode(data, ix);
               return data[ix].item;
            })
            .collect(Collectors.toList());
   }

   private void checkSampleSize(int k, boolean withReplacement)
   {
      if (k <= 0)
      {
         String msg = MessageFormat.format(ERR_MESSAGE_SAMPLE_SIZE_LESS_THAN_ZERO, k);
         throw new IllegalArgumentException(msg);
      }

      if (k > nodes.length && !withReplacement)
      {
         String msg = MessageFormat.format(ERR_MESSAGE_SAMPLE_SIZE_TO_BIG, k, nodes.length);
         throw new IllegalArgumentException(msg);
      }
   }

   /**
    * Selects a node from the heap based on a supplied weight.
    */
   private static <X> int select(WeightedItem<X>[] nodes, double weight)
   {
      int ix = 1;
      while (weight > nodes[ix].weight)
      {
         weight = weight - nodes[ix].weight;
         ix = ix << 1;
         if (weight > nodes[ix].totalWeight)
         {
            weight = weight - nodes[ix].totalWeight;
            ix += 1;
         }
      }

      return ix;
   }

   /**
    * Sets the weight of the identified node node to zero, so that it will not be selected
    * in subsequent calls.
    */
   private static <X> void removeNode(WeightedItem<X>[] nodes, int ix)
   {
      // make sure this node isn't chosen again.
      double wt = nodes[ix].weight;
      nodes[ix].weight = 0;
      while (ix > 0)
      {
         nodes[ix].totalWeight -= wt;
         ix = ix >> 1;
      }
   }

   private static final class WeightedItem<T>
   {
      public T item;
      public double weight;
      public double totalWeight;

      public WeightedItem(T item, ToDoubleFunction<T> adapter)
      {
         this.item = item;
         this.weight = adapter.applyAsDouble(item);
         this.totalWeight = this.weight;
      }
   }

}

package net.sf.taverna.biocatalogue.ui;

/**
 * 
 * @author Sergejs Aleksejevs
 *
 */
public interface HistoryOrFavoritesBlockEntryDetailsProvider
{
  /**
   * This method is required to produce three components that are
   * necessary to place an object into the history or favourites block.
   * 
   * The reason for extraction of this method from <code>HistoryOfFavouritesBlock</code>
   * class is that the parts for all objects are identical, but contents vary - this
   * way <code>HistoryOfFavouritesBlock</code> is focused just on the UI, instead of
   * on interpreting various types of objects.
   */
  HistoryOrFavouritesBlock.Entry provideEntryDetails(HistoryOrFavouritesBlock displayPanel,
      Object objectToProvideDetailsFor, int indexOfObjectInDisplayPanelDataCollection);
}

module Ex2 where

import Ex1

{-
  N.B. folding a ListBag with a binary function should apply the function to the elements of the multiset,
  ignoring the multiplicities.
 -}
instance Foldable ListBag where
    -- foldr :: Foldable t => (a -> b -> b) -> b -> t a -> b
    foldr f base (LB bag) = foldr (\pair base -> f (fst pair) base) base bag

{-
  applies the function 'f' to each element of the passed ListBag
  (more precisely only on the first component of each pair, since f is an unary function).
 -}
mapLB :: (a1 -> a2) -> ListBag a1 -> ListBag a2
mapLB f (LB bag) = LB (map (\pair -> (f (fst pair), snd pair)) bag)

{-
  WHY IS NOT POSSIBLE TO DEFINE AN INSTANCE OF 'Functor' FOR 'ListBag',
  BY PROVIDING 'mapLB' AS THE IMPLEMENTATION OF 'fmap'?

  "mapLB" simply applies a function to all the first components of the elements of the ListBag:
  for some functions, if they're applied to a well-formed ListBag, the returned one can lose the well-formedness property
  (like multiplying by a constants, ex. *0).
  Since the Functor must not change the structure (and the properties) of the type that is mapping,
  we cannot instantiate a Functor over ListBag using mapLB as implementation of 'fmap'.
-}
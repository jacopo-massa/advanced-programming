module Ex1 where

{-
   ListBag type constructor is used to implement Multisets.
   A ListBag contains a list of pairs (v,n):
     - 'v' is the element of the multiset,
     - 'n' is the multiplicity of 'v', defined as the number of occurrences of an element in the multiset.
 -}
data ListBag a = LB [(a, Int)]
  deriving (Show, Eq)


{-
  auxiliary function to add 'occ' times the element 'v' into an existing ListBag.
  If 'v' already belongs to the ListBag, its multiplicity is incremented,
  or a new pair (v,1) is added otherwise.
 -}
add :: Eq a => a -> Int -> ListBag a -> ListBag a
add v occ (LB []) = LB [(v,occ)]
add v occ (LB ((v', n):xs))
  | v == v'   = LB ((v', n + occ):xs)
  | otherwise = let LB rest = add v occ (LB xs) in LB ((v', n) : rest)

{-
  predicate that returns if the passed ListBag is well-formed, so
  if it does not contain two pairs (v, k) and (v', k') with v = v'.
  Property is checked by folding the list of pairs into the ListBag
 -}
wf :: Eq a => ListBag a -> Bool
wf bag = wf' bag
  where
    wf' (LB []) = True
    wf' (LB (x:xs)) = if (foldl (\base item -> base || (fst(x) == fst(item))) False xs)
                      then False else (wf' (LB xs))


{- CONSTRUCTORS -}

-- returns an empty ListBag
empty :: ListBag a
empty = LB []

-- returns a ListBag containing just one occurrence of 'v'
singleton :: a -> ListBag a
singleton v = LB [(v,1)]

{-
  returns a ListBag containing all the distinct elements of 'lst',
  with multiplicity = #times the element appears in 'lst'
 -}
fromList :: (Foldable t, Eq a) => t a -> ListBag a
fromList lst = foldl (\bag x -> add x 1 bag) empty lst


{- OPERATIONS -}

-- check if 'bag' is empty (so if the list of pairs into it is empty)
isEmpty :: ListBag a -> Bool
isEmpty bag = isEmpty' bag
  where
    isEmpty' (LB []) = True
    isEmpty' (LB xs) = False

-- if 'v' is an element of 'bag', returns the multiplicity of 'v', 0 otherwise
mul :: Eq a => a -> ListBag a -> Int
mul v bag = mul' v bag
  where
    mul' v (LB []) = 0
    mul' v (LB ((v', n):xs)) = if v == v' then n else (mul v (LB xs))

-- convert 'bag' into a list containing all the elements of 'bag', each one repeated #times = its multiplicity
toList :: ListBag a -> [a]
toList bag = toList' bag
  where
    toList' (LB []) = []
    toList' (LB ((v, n):xs)) = concat [(take n (repeat v)), (toList' (LB(xs)))]

-- adds all the elements of bag' into bag
sumBag :: Eq a => ListBag a -> ListBag a -> ListBag a
sumBag bag bag' = sumBag' bag bag'
  where
    sumBag' bag (LB []) = bag
    sumBag' (LB []) bag' = bag'
    sumBag' bag (LB ((y,n):ys)) = let LB rest = add y n bag in sumBag' (LB rest) (LB ys)


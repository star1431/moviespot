function Skeleton({ className = '', ...props }) {
  return (
    <div
      className={`animate-pulse rounded-lg bg-gray-200 ${className}`}
      {...props}
    />
  );
}

export default Skeleton;
export { Skeleton };

export function MovieCardSkeleton() {
  return (
    <div className="overflow-hidden rounded-lg bg-white shadow-sm">
      <Skeleton className="aspect-[2/3] w-full" />
      <div className="p-3">
        <Skeleton className="mb-2 h-4 w-3/4" />
        <Skeleton className="h-3 w-1/2" />
      </div>
    </div>
  );
}

export function MovieGridSkeleton({ count = 20 }) {
  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
      {Array.from({ length: count }).map((_, i) => (
        <MovieCardSkeleton key={i} />
      ))}
    </div>
  );
}


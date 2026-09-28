
-- The table to store player moves
CREATE TABLE IF NOT EXISTS "movements" (

    -- The timestamp where it was emitted
    "timestamp" INTEGER NOT NULL,

	-- The account that made the move, to differentiate clients
	"emitter" TEXT NOT NULL,

	-- The east/west coord
	"abscissa" INTEGER NOT NULL,

	-- The north/south component
	"ordinate" INTEGER NOT NULL,

	-- The floor
	"floor" INTEGER NOT NULL
);

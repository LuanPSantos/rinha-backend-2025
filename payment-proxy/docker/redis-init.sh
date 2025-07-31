#!/usr/bin/env sh
# 1) start Redis in the background
redis-server --save '' --appendonly no --maxclients 20000 &

# 2) wait for Redis to be ready
until redis-cli ping > /dev/null 2>&1; do
  echo "Waiting for Redis..."
  sleep 1
done

# 3) create the consumer group (MKSTREAM: will also create the stream if missing)
#    note: the '$' must be quoted so the shell doesn’t try to expand it
redis-cli XGROUP CREATE pending payment-group '$' MKSTREAM || \
  echo "Group already exists, continuing…"

# 4) bring Redis back to foreground (so container lives on redis-server PID 1)
wait

+ ContiguousBlockAllocator {

	uReserve { |address, size = 1, warn = true|
		var block = array[address - addrOffset] ?? { this.prFindNext(address) };
		var new;
		if(block.notNil and:
			{ block.used and:
				{ address + size > block.start }
		}) {
			if(warn) {
				"The block at (%, %) is already in use and cannot be reserved."
				.format(address, size).warn;
			};
		} {
			if(block.notNil and: { block.start == address }) {
				new = this.prReserve(address, size, block);
				^new
			} {
				block = this.prFindPrevious(address);
				if(block.notNil and:
					{ block.used and:
						{ block.start + block.size > address }
				}) {
					if(warn) {
						"The block at (%, %) is already in use and cannot be reserved."
						.format(address, size).warn;
					};
				} {
					new = this.prReserve(address, size, nil, block);
					^new
				};
			};
		};
		^nil
	}

}
.PHONY: build load deploy restart clean

IMAGE_NAME := merchant-payout-system
IMAGE_TAG := dev
KIND_CLUSTER := local-cluster

build:
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .

load: build
	kind load docker-image $(IMAGE_NAME):$(IMAGE_TAG) --name $(KIND_CLUSTER)

clean:
	docker rmi $(IMAGE_NAME):$(IMAGE_TAG)

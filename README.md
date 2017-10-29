# AWS CodeCommit Jobs

This plugin auto-discovers the repositories in code commit.

## How to configure the plugin

First, we need to create a **AWS CodeCommit Jobs** project.

![configuration](.misc/create.png)


Then we can configure the access to aws code commit:
* URL API of code commit
* Project name's pattern
* Credentials for API (AMI)
* Credentials for git code commit (AMI)
* Git Behaviours by default we consider all the branches.

![configuration](.misc/configure.png)


## How to play

We must have the toys:

* docker
* docker-compose
* make

```bash
make help
```

### Configuration

We should create a file awscodecommit.env.

```properties
AMI_ACCESS=<AMI access>
AMI_SECRET=<AMI secret>

GIT_USER=<GIT HTTP username>
GIT_PASSWORD=<GIT HTTP password>
```

* [Amazon HTTPS Git](http://docs.aws.amazon.com/codecommit/latest/userguide/how-to-share-repository.html?icmpid=docs_acc_console_intro#how-to-share-repository-cli)

### Docker

We have a docker with all needed dependencies.

## TODOs

* Refactor code to manage AWSCodeCommit client.
* Do More unit tests / integration test.
* Manage the failures.
* Add images.
* Add commands line to create ami jenkins and add https git access.


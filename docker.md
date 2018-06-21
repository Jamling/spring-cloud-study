# Docker 安装

`# yum install docker`

## 可选步骤（Ubuntu）

安装完成后根据提示，可以将当前用户加到 docker 用户组里，这样就不用每次
执行 docker 都需要 sudo 了。
sudo usermod -aG docker <你的用户名>

注，执行上面的操作后需重启docker

`# service docker restart`


# 启动docker
- service 命令的用法
`# service docker start`

- systemctl 命令的用法
`# systemctl start docker`

# 查看docker版本

```
[root@w]# docker version
Client:
 Version:         1.13.1
 API version:     1.26
 Package version: docker-1.13.1-63.git94f4240.el7.centos.x86_64
 Go version:      go1.9.4
 Git commit:      94f4240/1.13.1
 Built:           Fri May 18 15:44:33 2018
 OS/Arch:         linux/amd64

Server:
 Version:         1.13.1
 API version:     1.26 (minimum version 1.12)
 Package version: docker-1.13.1-63.git94f4240.el7.centos.x86_64
 Go version:      go1.9.4
 Git commit:      94f4240/1.13.1
 Built:           Fri May 18 15:44:33 2018
 OS/Arch:         linux/amd64
 Experimental:    false

```

# 镜像加速

我选择的镜像是registry.docker-cn.com

使用永久性修改

修改 `/etc/docker/daemon.json` 文件并添加上 registry-mirrors 键值。

{
  "registry-mirrors": ["https://registry.docker-cn.com"]
}

修改保存后重启 Docker 以使配置生效

# 搭建私有仓库

## 服务端

```
docker run -d -p 5000:5000 -v /opt/data/registry:/var/lib/registry --name registry --restart always registry
```
允许删除无用的镜像
需要编辑容器内的/etc/docker/registry/config.yml文件，添加

```yml
storage:
  delete: 
    enable: true
```
进入容器并编辑
```
docker exec -it registry /bin/sh
/ # vi /etc/docker/registry/config.yml
```

## 客户端

首先要修改设置，允许以http（默认是https）协议访问私有仓库, 不同的系统，配置文件不一样。以Ubuntu为例
我修改的是`/etc/default/docker`，添加了以下内容
`DOCKER_OPTS="--insecure-registry 192.168.0.153:5000"`

centos的配置文件好像是/etc/sysconfig/docker

修改完，重启生效。

## 客户端上传镜像

### 准备本地镜像

这里不真实生成镜像。简单一点，从官方镜像pull一个比较小的镜像来测试（此处使用的是busybox）。

`docker pull busybox`

接下来修改一下该镜像的tag。

`docker tag busybox 192.168.0.153:5000/busybox`

接下来把打了tag的镜像上传到私有仓库。

`docker push 192.168.0.153:5000/busybox`

### 查看私有仓库中的镜像

`# curl -XGET http://registry:5000/v2/_catalog`

查看镜像版本

`# curl -XGET http://registry:5000/v2/image_name/tags/list`

# docker 常用命令

－docker run

－docker rmi

－docker stop

－docker logs

以下命令请加--help查看

# 安装docker-compose
跑去github下载一个
https://github.com/docker/compose/releases
将下载的文件copy并重命名为/usr/local/bin/docker-compose

```sh
# chmod +x /usr/local/bin/docker-compose
# docker-compose --version
# docker-compose version 1.21.0, build 5920eb0
```

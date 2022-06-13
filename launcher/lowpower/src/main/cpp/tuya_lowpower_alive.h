/*
 * tuya_lowpower_alive.h
 *Copyright(C),2017-2022, TUYA company www.tuya.com
 *
 *FILE description:
  *
 *  Created on: 2021年10月11日
 *      Author: kuiba
 */

#ifndef TY_IPC_LOW_POWER_SDK_SDK_SVC_LOWPOWER_DEMO_EXT_INCLUDE_TUYA_LOWPOWER_ALIVE_H_
#define TY_IPC_LOW_POWER_SDK_SDK_SVC_LOWPOWER_DEMO_EXT_INCLUDE_TUYA_LOWPOWER_ALIVE_H_
#ifdef __cplusplus
extern "C" {
#endif

typedef int (*wakeup_fun_ptr)(int index);
typedef int (*alive_socket_close_ptr)(int index,int result);

typedef struct {
    wakeup_fun_ptr  wakeup_callback;//唤醒回调函数。index参数是tuya_ipc_lowpower_alive_add的第一个参入的值。表示第几路设备。范围[0,200）,最大支持200路
    alive_socket_close_ptr close_socket_callback;//包活链路关闭回调。index参数同wakup_fun_ptr。第二个参数result表示链路是否正常关闭。0表示正常关闭，比如主进程拉起，包活链路被主动踢掉的情况。非零，表示异常。此时用户可以重新add 包活
    int alive_time_interval;//包活时间，单位是秒。范围在60到150之间[60,150).合理的值在50到120之间。
}TUYA_LOWPOWER_ALIVE_CTX_S;
/*
 *  tuya_ipc_lowpower_alive_init 初始化低功耗模块，只允许初始化一次。
 *  TUYA_LOWPOWER_ALIVE_CTX_S：参考数据结构说明
 */
int tuya_ipc_lowpower_alive_init(TUYA_LOWPOWER_ALIVE_CTX_S *alive_ctx);
/*
 *  tuya_ipc_lowpower_alive_add 添加包活设备。保证轻量，单线程操作。
 *  index:参考TUYA_LOWPOWER_ALIVE_CTX_S中index说明。
 *  serverIp：低功耗服务器IP
 *  port:包活端口
 *  pdevID：设备ID
 *  idLen:设备ID长度
 *  pkey：local key
 *  keyLen:local key len
 */
int tuya_ipc_lowpower_alive_add(int index,unsigned int serverIp,signed int port,char* pdevId, int idLen, char* pkey, int keyLen);
#ifdef __cplusplus
}
#endif
#endif /* TY_IPC_LOW_POWER_SDK_SDK_SVC_LOWPOWER_DEMO_EXT_INCLUDE_TUYA_LOWPOWER_ALIVE_H_ */

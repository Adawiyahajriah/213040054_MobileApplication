package id.ac.unpas.agenda.repositories

import androidx.annotation.WorkerThread
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import com.skydoves.whatif.whatIf
import com.skydoves.whatif.whatIfNotNull
import id.ac.unpas.agenda.models.Todo
import id.ac.unpas.agenda.networks.TodoApi
import id.ac.unpas.agenda.persistences.TodoDao
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TodoRepository @Inject constructor(private val api: TodoApi, private val dao: TodoDao) {

    @WorkerThread
    fun loadItems(onSuccess: () -> Unit,
                  onError: (String) -> Unit) = flow {
        val list: List<Todo> = dao.findAll()
        api.findAll()
            .suspendOnSuccess {
                 data.whatIfNotNull {
                     dao.upsert(it.data)
                     val localList = dao.findAll()
                     emit(localList)
                     onSuccess()
                 }
            }
            .suspendOnError {
                emit(list)
                onError(message())
            }
            .suspendOnException {
                emit(list)
                onError(message())
            }
    }

    suspend fun insert(todo: Todo,
                       onSuccess: () -> Unit,
                       onError: (String) -> Unit) {
        dao.upsert(todo)
        api.insert(todo)
            .suspendOnSuccess {
                data.whatIfNotNull {
                    if (it.success) {
                        onSuccess()
                    } else {
                        onError(it.message)
                    }
                }
            }
            .suspendOnError {
                onError(message())
            }
            .suspendOnException {
                onError(message())
            }
    }

    suspend fun update(todo: Todo,
                       onSuccess: () -> Unit,
                       onError: (String) -> Unit) {
        dao.upsert(todo)
        api.update(todo.id, todo)
            .suspendOnSuccess {
                data.whatIfNotNull {
                    if (it.success) {
                        onSuccess()
                    } else {
                        onError(it.message)
                    }
                }
            }
            .suspendOnError {
                onError(message())
            }
            .suspendOnException {
                onError(message())
            }
    }

    suspend fun delete(id: String,
                       onSuccess: () -> Unit,
                       onError: (String) -> Unit) {
        dao.delete(id)
        api.delete(id)
            .suspendOnSuccess {
                data.whatIfNotNull {
                    if (it.success) {
                        onSuccess()
                    } else {
                        onError(it.message)
                    }
                }
            }
            .suspendOnError {
                onError(message())
            }
            .suspendOnException {
                onError(message())
            }
    }

    suspend fun find(id: String) = dao.find(id)
}
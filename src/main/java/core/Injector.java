package core;

import annotations.Inject;
import exception.InjectException;
import loader.ModuleClassLoader;
import structure.ModuleConfig;
import utils.Scanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 理想的程序结构：
 * 模块文件：			  ...workPath/
 * 模块java文件编译后：  ...compiledPath/classes/
 * 模块依赖jar包编译后： ...compiledPath/lib/
 */
public class Injector {

	private static Injector injector;

	public static Injector getInjector() {
		return injector;
	}

	// 关联唯一类加载器
	public ClassLoader classLoader;

	// 通过模块配置文件关联模块
	public ModuleConfig moduleConfig;

	// 单件缓存
	private Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();

	// 命名单件缓存
	private Map<Class<?>, Map<Annotation, Object>> qualifiedInstances = new ConcurrentHashMap<>();
	{
		singletonInstances.put(Injector.class, this);
	}

	// 单件类表
	private Map<Class<?>, Class<?>> singletonClasses = new ConcurrentHashMap<>();

	public Collection<Class<?>> getCells() {
		return singletonClasses.values();
	}

	// 命名单件类表
	private Map<Class<?>, Map<Annotation, Class<?>>> qualifiedClasses = new ConcurrentHashMap<>();

	// 解决循环注入的缓存
	private Map<Class<?>, Object> earlyInstances = new ConcurrentHashMap<>();

	// 几个常用注解的本地类加载器缓存
	Class<? extends Annotation> namedAnno = null;
	Class<? extends Annotation> singletonAnno = null;
	Class<? extends Annotation> InjectAnno = null;

	/**
	 * 不新建自定义类加载器
	 */
	public Injector() {
		this.classLoader = ClassLoader.getSystemClassLoader();
		scanAndLoadClasses();
		injector = this;
	}

	/**
	 * 构造器
	 * @param urls 资源目录
	 */
	public Injector(URL[] urls) {
		// packageName like "core/base"
		String classPath = urls[0].getPath().substring(1);
		String workPath = Thread.currentThread().getContextClassLoader().getResource("").getPath().substring(1);
		this.classLoader = new ModuleClassLoader(urls, null, workPath);
		scanAndLoadClasses(classPath);

		injector = this;
	}

	/**
	 * 构造器
	 * 从给定模块包名自动分析模块的绝对地址，并检索模块下的jar包绝对地址，合并作为资源目录构造类加载器
	 * @param packageName 模块（程序）包名
	 */
	public Injector(String packageName) {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		String workPath = currentClassLoader.getResource("").getPath();

		// 确定所有的类加载器目录，并初始化类加载器
		List<String> classCatalog = scanJars(workPath + packageName);
		if (classCatalog == null) {
			classCatalog = new ArrayList<>();
		}
		classCatalog.add(workPath);
		URL[] urls = new URL[classCatalog.size()];
		int var1 = 0;
		for (String catalog : classCatalog) {
			try {
				System.out.println("URL路径： " + catalog);
				urls[var1++] = new URL("file:/" + catalog);
			} catch (Exception e) {
			}
		}
		this.classLoader = new ModuleClassLoader(urls, null);

		// 开始扫描并加载模块（包括jar包）中的类
		String modulePath = workPath.concat(packageName);
		scanAndLoadClasses(modulePath);

		injector = this;
	}

	/**
	 * 扫描所有的jar包
	 * 此处规定必须放在模块工作目录下的lib包内
	 * @param packageName 包名
	 * @return 模块内所有jar包的绝对路径的集合
	 */
	private List<String> scanJars(String packageName) {
		String jarName = packageName.substring(0,
				packageName.substring(0, packageName.length() - 1).lastIndexOf("classes")).concat("lib/");
		//System.out.println("扫描lib: " + jarName);
		return Scanner.getClassName(jarName, true);
	}

	/**
	 * 扫描并加载jar包内所有类并加载到类加载器中
	 * @param jarsList jar包绝对路径的集合
	 */
	public void scanAndLoadJars(List<String> jarsList) {
		String jarPath = null;
		for (int i = 0; i < jarsList.size() - 1; i++) {
			jarPath = jarsList.get(i);
			List<String> jarClassNames = Scanner.getClassNameByJar(jarPath, true);
			for (String jarClass : jarClassNames) {
				try {
					classLoader.loadClass(jarClass);
				} catch (Exception e) {
					// ignored
				}
			}
		}
	}

	/**
	 * 扫描模块目录下的所有类并加载到自定义加载器中
	 * @param modulePath 模块的绝对路径
	 */
	public void scanAndLoadClasses(String modulePath) {
		// 切换类加载器
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		// 获取程序目录下所有类
		List<String> classNames = Scanner.getClassName(modulePath, true);
		List<Class<?>> set = new ArrayList<Class<?>>();
		if (classNames != null) {
			for (String className : classNames) {
				try {
					Class<?> clazz = classLoader.loadClass(className);
					set.add(clazz);
				} catch (ClassNotFoundException ignored) {
				}
			}
		}

		// 加载几个常用注解
		try {
			namedAnno = (Class<? extends Annotation>) classLoader.loadClass("annotations.Named");
			singletonAnno = (Class<? extends Annotation>) classLoader.loadClass("annotations.Singleton");
			InjectAnno = (Class<? extends Annotation>) classLoader.loadClass("annotations.Inject");
		} catch (Exception e) {
			// ignore
		}

		// 把加载的类注册到单例表和命名单例表中
		for (Class<?> clazz : set) {
			// 扫描类注解
			Annotation[] annotations = clazz.getAnnotations();
			Annotation namedAnnotation = null;
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == singletonAnno || annotation.annotationType() == namedAnno) {
					namedAnnotation = annotation;
				}
			}
			if (namedAnnotation == null) {
				continue;
			}
			// 注册
			if (clazz.isAnnotationPresent(namedAnno)) {
				//System.out.println("注册到分类表" + clazz);
				Class<?>[] inters = clazz.getInterfaces();
				for (Class<?> inter : inters) {
					registerQualifiedClass(inter, namedAnnotation, clazz);
				}
			}
			if (clazz.isAnnotationPresent(singletonAnno)) {
				//System.out.println("注册到类表");
				registerSingletonClass(clazz, clazz);
			}
		}
		// 切换回原来的类加载器
		Thread.currentThread().setContextClassLoader(currentClassLoader);
	}

	public void scanAndLoadClasses() {
		scanAndLoadClasses("");
	}

	/**
	 * 把生成的单件放入单件类表
	 * @param clazz 单件的类型
	 * @param obj 生成的单件实例
	 * @return 注入器
	 */
	public <T> Injector putSingleton(Class<T> clazz, T obj) {
		if (singletonInstances.put(clazz, obj) != null) {
			throw new InjectException("duplicated singleton object for the same class " + clazz.getCanonicalName());
		}
		return this;
	}

	/**
	 * 把生成的单件放入命名单件类表
	 * @param clazz 单件的类型
	 * @param annotation 单件的注解，用来标注分类
	 * @param obj 生成的单件实例
	 * @return 注入器
	 */
	public <T> Injector putQualified(Class<T> clazz, Annotation annotation, T obj) {
		if (!annotation.annotationType().isAnnotationPresent(namedAnno)) {
			throw new InjectException(
					"annotation must be decorated with Qualifier " + annotation.annotationType().getCanonicalName());
		}
		var os = qualifiedInstances.get(clazz);
		if (os == null) {
			os = new ConcurrentHashMap<>();
			qualifiedInstances.put(clazz, os);
		}
		if (os.put(annotation, obj) != null) {
			throw new InjectException(
					String.format("duplicated qualified object with the same qualifier %s with the class %s",
							annotation.annotationType().getCanonicalName(), clazz.getCanonicalName()));
		}
		return this;
	}

	/**
	 * 注册单件
	 * @param clazz 单件的类型
	 * @return 注入器
	 */
	public <T> Injector registerSingletonClass(Class<T> clazz) {
		return this.registerSingletonClass(clazz, clazz);
	}

	/**
	 * 注册单件
	 * parentType为预留功能，通常在面向父类或接口时使用registerQualifiedClass方法
	 * @param parentType 单件的父类或者接口
	 * @param clazz 单件的类型
	 * @return 注入器
	 */
	public <T> Injector registerSingletonClass(Class<?> parentType, Class<T> clazz) {
		if (singletonClasses.put(parentType, clazz) != null) {
			throw new InjectException("duplicated singleton class " + parentType.getCanonicalName());
		}
		return this;
	}

	/**
	 * 注册命名单件
	 * @param parentType 单件的父类或者接口
	 * @param annotation 单件的注解，用来标注分类
	 * @param clazz 单件的类型
	 * @return 注入器
	 */
	public <T> Injector registerQualifiedClass(Class<?> parentType, Annotation annotation, Class<T> clazz) {
		if (annotation.annotationType() != namedAnno) {
			throw new InjectException(
					"annotation must be decorated with Qualifier " + annotation.annotationType().getCanonicalName());
		}
		var annos = qualifiedClasses.get(parentType);
		if (annos == null) {
			annos = new ConcurrentHashMap<>();
			qualifiedClasses.put(parentType, annos);
		}
		if (annos.put(annotation, clazz) != null) {
			throw new InjectException(String.format("duplicated qualifier %s with the same class %s",
					annotation.annotationType().getCanonicalName(), parentType.getCanonicalName()));
		}
		return this;
	}

	public <T> Injector registerQualifiedClass(Class<?> parentType, Class<T> clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			if (anno.annotationType().isAnnotationPresent(namedAnno)) {
				return this.registerQualifiedClass(parentType, anno, clazz);
			}
		}
		throw new InjectException("class should decorated with annotation tagged by Qualifier");
	}

	/**
	 * 打印单件类表
	 */
	public void printSingletonClasses() {
		for (Class<?> clazz : singletonClasses.keySet()) {
			System.out.println(clazz + " -> " + singletonClasses.get(clazz));
		}
	}

	/**
	 * 打印命名单件类表
	 */
	public void printQualifiedClasses() {
		for (Class<?> clazz : qualifiedClasses.keySet()) {
			Map<Annotation, Class<?>> value = qualifiedClasses.get(clazz);
			for (Annotation annotation : value.keySet()) {
				System.out.println(clazz + " : { " + annotation.toString() + " -> " + value.get(annotation).getSimpleName() + "}");
			}
		}
	}

	/**
	 * 从命名单件类表中创建
	 * @param declaringClazz the Class calling this function
	 * @param clazz Class type for creating new instance from qualifiedInstances or qualifiedClasses Dict
	 * @param annotations annotation for identifying (usually by name) target corresponding clazz
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createFromQualified(Class<?> declaringClazz, Class<?> clazz, Annotation[] annotations) {
		var qs = qualifiedInstances.get(clazz);
		if (qs != null) {
			Set<Object> os = new HashSet<>();
			for (var annotation : annotations) {
				var obj = qs.get(annotation);
				if (obj != null) {
					os.add(obj);
				}
			}
			if (os.size() > 1) {
				throw new InjectException(String.format("duplicated qualified object for field %s@%s",
						clazz.getCanonicalName(), declaringClazz.getCanonicalName()));
			}
			if (!os.isEmpty()) {
				return (T) (os.iterator().next());
			}
		}
		// 此处需要预处理，先将相关类扫描进qualifiedClasses哈希表中
		var qz = qualifiedClasses.get(clazz);
		if (qz != null) {
			Set<Class<?>> oz = new HashSet<>();
			Annotation annoz = null;
			for (var annotation : annotations) {
				var z = qz.get(annotation);
				if (z != null) {
					oz.add(z);
					annoz = annotation;
				}
			}
			if (oz.size() > 1) {
				throw new InjectException(String.format("duplicated qualified classes for field %s@%s",
						clazz.getCanonicalName(), declaringClazz.getCanonicalName()));
			}
			if (!oz.isEmpty()) {
				final var annozRead = annoz;
				// 生成新的对象以后放到qualified队列里
				var t = (T) createNew(oz.iterator().next(), (o) -> {
					this.putQualified((Class<T>) clazz, annozRead, (T) o);
				});
				return t;
			}
		}
		return null;
	}

	/**
	 *
	 * @param clazz Class type for creating new instance
	 * @return new instance
	 */
	private <T> T createNew(Class<T> clazz) {
		return this.createNew(clazz, null);
	}

	/**
	 *
	 * @param clazz Class type for creating new instance
	 * @param consumer operation after creating new instance
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createNew(Class<T> clazz, Consumer<T> consumer) {
		ClassLoader tmpClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
		var o = singletonInstances.get(clazz);
		if (o != null) {
			return (T) o;
		}
		var cons = new ArrayList<Constructor<T>>();
		T target = null;
		// 1. 创建对象
		for (var con : clazz.getDeclaredConstructors()) {
			// 默认和无参构造器不需要"@Inject"注解
			if (!con.isAnnotationPresent(InjectAnno) && con.getParameterCount() > 0) {
				continue;
			}
			if (!con.trySetAccessible()) {
				continue;
			}
			cons.add((Constructor<T>) con);
		}
		if (cons.size() > 1) {
			throw new InjectException("duplicated constructor for injection class " + clazz.getCanonicalName()); // 按规范不允许有超过一个构造器添加"@Inject"标签
		}
		if (cons.size() == 0) {
			throw new InjectException("no accessible constructor for injection class " + clazz.getCanonicalName());
		}
		earlyInstances.put(clazz, clazz); // 早期对象尚未创建

		target = createFromConstructor(cons.get(0)); // -> 核心步骤，构造器注入

		earlyInstances.put(clazz, target); // 早期对象创建成功

		// 2. 创建完成，判断是否为Singleton
		var isSingleton = clazz.isAnnotationPresent(singletonAnno);
		if (!isSingleton) {
			isSingleton = this.singletonClasses.containsKey(clazz);
		}
		if (isSingleton) {
			singletonInstances.put(clazz, target);
		}

		// 3. 递归注入该类中带@Inject注解的属性
		injectMembers(target);

		earlyInstances.remove(clazz);

		Thread.currentThread().setContextClassLoader(tmpClassLoader);
		return target;
	}

	/**
	 *
	 * @param constructor the constructor used for creating new instance
	 * @return new instance
	 */
	private <T> T createFromConstructor(Constructor<T> constructor) {
		var params = new Object[constructor.getParameterCount()];
		var i = 0;
		for (Parameter parameter : constructor.getParameters()) {
			Object param = null;
			if (parameter.getClass().isInterface()) {
				throw new InjectException(String.format("can not create instance form Interface, the root class is %s",constructor.getDeclaringClass().getCanonicalName()));
			}
			// 循环依赖对象尚未构建早期引用，此处循环依赖无法解决
			if (earlyInstances.get(parameter.getType()) == parameter.getType()) {
				throw new InjectException(String.format("circular dependency on constructor , the root class is %s",constructor.getDeclaringClass().getCanonicalName()));
			}
			// 循环依赖对象已经构建早期对象
			if (earlyInstances.containsKey(parameter.getType())) {
				param = earlyInstances.get(parameter.getType());
			}
			else {
				param = createFromParameter(parameter);
			}
			params[i++] = param;
		}
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			throw new InjectException("create instance from constructor error", e);
		}
	}

	/**
	 *
	 * @param parameter from this parameter creating new instance
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createFromParameter(Parameter parameter) {
		var clazz = parameter.getType();
		// 从缓存队列中创建
		T t = createFromQualified(parameter.getDeclaringExecutable().getDeclaringClass(), clazz,
				parameter.getAnnotations());
		if (t != null) {
			return t;
		}
		return (T) createNew(clazz);
	}

	/**
	 *
	 * @param field from this field creating new instance
	 * @return new instance
	 */
	@SuppressWarnings("unchecked")
	private <T> T createFromField(Field field) {
		var clazz = field.getType();
		// 从缓存队列中创建
		T t = createFromQualified(field.getDeclaringClass(), field.getType(), field.getAnnotations());
		if (t != null) {
			return t;
		}
		else
			return (T) createNew(clazz);
	}

	/**
	 * inject fields of a new instance after that being created
	 * @param instance an instance whose fields waiting for injection
	 */
	public <T> void injectMembers(T instance) {
		List<Field> fields = new ArrayList<>();
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(InjectAnno) && field.trySetAccessible()) {
				fields.add(field);
			}
		}
		for (Field field : fields) {
			try {
				if (field.get(instance) != null && field.isAnnotationPresent(singletonAnno)) {
					// 该成员变量为单例且已经被构造器创建（于目标对象初始化时），跳过
					continue;
				}
				Class<?> clazz = field.getType();
				Annotation[] annotations = field.getDeclaredAnnotations();
				Annotation namedAnnotation = null;
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().isAnnotationPresent(namedAnno)) {
						namedAnnotation = annotation;
					}
				}
				// 0. 若循环依赖，先把构建好的早期对象引用赋予
				Object obj = earlyInstances.get(clazz);
				if (obj == clazz) {
					throw new InjectException("circle dependent from constructor param" +
							instance.getClass().getSimpleName() + " asking for" + clazz.getSimpleName() + "in building");
				}
				// 1. 尝试从singletonInstances队列中获取
				if (obj == null) {
					obj = singletonInstances.get(clazz);
				}
				// 2. 尝试从qualifiedInstances队列中获取
				if (obj == null) {
					Map<Annotation, Object> objectMap = qualifiedInstances.get(clazz);
					if (objectMap != null) {
						obj = objectMap.get(namedAnnotation);
					}
				}
				// 3. 都没有，重新创建一个
				if (obj == null) {
					obj = createFromField(field);
				}
				// 将生成的实例放入singletonInstances队列
				if (field.isAnnotationPresent(singletonAnno)) {
					singletonInstances.put(clazz, obj);
				}
				// 将生成的实例放入qualifiedInstances队列
				if (namedAnnotation != null) {
					Map<Annotation, Object> instanceMap = qualifiedInstances.getOrDefault(clazz, new HashMap<>());
					instanceMap.put(namedAnnotation, obj);
					qualifiedInstances.put(clazz, instanceMap);
				}
				field.set(instance, obj);
			} catch (Exception e) {
				throw new InjectException(
						String.format("set field for %s@%s error", instance.getClass().getCanonicalName(), field.getName()), e);
			}
		}
	}

	/**
	 * get a new instance of clazz
	 * @param clazz class type of target
	 * @return new instance
	 */
	public <T> T getInstance(Class<T> clazz) {
		Class<?> clazz0 = null;
		try {
			clazz0 = classLoader.loadClass(clazz.getCanonicalName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (T) createNew(clazz0);
	}

	public <T> T getInstance(String name) {
		Class<?> clazz0 = null;
		try {
			clazz0 = classLoader.loadClass(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (T) createNew(clazz0);
	}


	public ModuleConfig getModuleConfig() {
		return moduleConfig;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}
}
